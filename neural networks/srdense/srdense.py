import time
import cv2

from contextlib import redirect_stdout
from data_generator import create_data_generator
# for the callbacks
from keras.callbacks import EarlyStopping, LearningRateScheduler, TensorBoard, ModelCheckpoint
# testing stuff
from keras.datasets import mnist
from keras.layers import (AveragePooling2D, BatchNormalization, Conv2D,
                          Conv2DTranspose, Dense, Flatten, Input, ReLU,
                          concatenate)
from keras.losses import mean_squared_error
from keras.models import Model
from keras.optimizers import Adam
from keras.regularizers import l2
from keras.utils import to_categorical
from keras.utils.vis_utils import plot_model

from memory_usage import get_model_memory_usage


def dense_layer(x, block_id, layer_id, filters=12, kernel_size=(3, 3), strides=(1, 1)):
    """
    This function will build a single dense layer of a dense block.

    inputs -- the outputs from the prev. layer
    filters -- how many conv. layers will be created
    kernel_size -- size of the conv. kernel
    strides -- the stride size of the kernel, should be (1,1) in most cases
    """
    x = Conv2D(filters=filters,
               strides=strides,
               kernel_size=kernel_size,
               padding='same',
               kernel_initializer='he_normal',
               use_bias=False,
               name=f"Block-{block_id}-Layer-{layer_id}-Conv2D")(x)
    x = ReLU(name=f"Block-{block_id}-Layer-{layer_id}-ReLU")(x)
    return x


def dense_block(x, block_id, layers=3, growth_rate=16, kernel_size=(3, 3), strides=(1, 1)):
    """
    This function will bild one dense block with k dense layers.

    inputs -- the outputs from the prev. layer
    layers -- how many dense layers should be in one dense block
    filters -- how many conv. layers will be created
    kernel_size -- size of the conv. kernel
    strides -- the stride size of the kernel, should be (1,1) in most cases
    """

    # add the k layers
    for i in range(layers):
        # create one dense layers
        cb = dense_layer(x, block_id, i, filters=growth_rate)
        # connected the output from all prev. ones
        if i == 0:
            x = cb
        else:
            x = concatenate([x, cb], axis=3, name=f"Block-{block_id}-InnerConcatenate-{i}")
    return x


def get_adam_optimizer(lr):
    """
    Return the adam optimizer

    lr -- learning rate
    """
    return Adam(lr)


DENSE_TYPE_H = 1
DENSE_TYPE_HL = 2
DENSE_TYPE_ALL = 3


def dense_model(dense_type, input_shape, blocks=[3, 4, 5], growth_rate=16, kernel_size=(3, 3), strides=(1, 1), weight_decay=1e-4):
    """
    This function will create the srdense net model.

    dense_type -- 1,2 or 3, use only High features, High and low, or all
    input_shape -- input shape of the input images
    blocks -- how many layers each dense block has
    growth_rate -- how many features are added per layer in one block
    kernel_size -- conv. kernel size
    strides -- strides size in x and y
    weight_decay -- for better learning
    """
    # define the input of the network
    inputs = Input(shape=input_shape, name="Input")

    # first extract the low level features, use 128 filters, because each
    # dense block will have the same size
    lowres = Conv2D(filters=128,
                    kernel_size=kernel_size,
                    kernel_initializer='he_normal',
                    padding='same',
                    strides=strides,
                    use_bias=False,
                    kernel_regularizer=l2(weight_decay),
                    name="LowLevelFeatures")(inputs)
    # add a ReLU after conv2D
    lowres = ReLU(name="LowLevelFeatures-ReLU")(lowres)

    concat = []
    # create all the denseblocks
    for i in range(len(blocks)):
        # if it is the first block, handle the output of low lowres
        if i == 0:
            # add one dense block
            x = dense_block(x=lowres,
                            block_id=i,
                            layers=blocks[i],
                            growth_rate=growth_rate)

            # if we need all denseblocks output concatenate with the low res ones
            # if not just continue with the output
            if dense_type == DENSE_TYPE_ALL:
                concat = concatenate([lowres, x], axis=3,
                                     name=f"Block-OuterConcatenate-{i}")
            else:
                concat = x
        else:
            # add one dense block
            x = dense_block(x=concat,
                            block_id=i,
                            layers=blocks[i],
                            growth_rate=growth_rate)
            # if we need all denseblocks output concatenate with the prev. dense blocks
            # if not just continue with the output
            if dense_type == DENSE_TYPE_ALL:
                concat = concatenate([concat, x], axis=3, name=f"Block-OuterConcatenate-{i}")
            else:
                concat = x

    # add the bottle neck, if we have all dense block concatenates, because
    # 1152 features would be quite a lot...
    # if we dont concatenate all, than if HL concatenate the lowres features
    if dense_type == DENSE_TYPE_ALL:
        x = Conv2D(filters=256,
                   kernel_size=(1, 1),
                   strides=(1, 1),
                   padding="same",
                   use_bias=False,
                   name="Bottleneck")(concat)
    elif dense_type == DENSE_TYPE_HL:
        x = concatenate([lowres, concat], axis=3, name=f"Block-OuterConcatenate-LH")

    # add the two deconv. layers
    # deconv. layers are mostly referred as ConvTranspose
    # this will make the network really big...
    x = Conv2DTranspose(filters=256,
                        kernel_size=(3, 3),
                        strides=(2, 2),
                        padding="same",
                        use_bias=False,
                        name="Deconvolution-1")(x)
    x = ReLU(name="Deconvolution-1-ReLU")(x)

    x = Conv2DTranspose(filters=256,
                        kernel_size=(3, 3),
                        strides=(2, 2),
                        padding="same",
                        use_bias=False,
                        name="Deconvolution-2")(x)
    x = ReLU(name="Deconvolution-2-ReLU")(x)

    # reconstruction layer
    outputs = Conv2D(filters=3,
                     kernel_size=(3, 3),
                     strides=(1, 1),
                     padding="same",
                     use_bias=False,
                     name="Reconstruction")(x)

    # create the model
    model = Model(input=inputs, output=outputs)

    # compile the model with the settings
    model.compile(loss=mean_squared_error,
                  optimizer=get_adam_optimizer(0.0001),
                  metrics=['accuracy'])
    return model


def update_lr(epoch, lr):
    """
    This function will adapt the learning rate like discussed in the paper.
    """
    if epoch == 30:
        lr /= 10
    return lr


def create_callbacks(name):
    """
    This function will create the callbacks during the fitting process.

    name -- name of the trail
    """

    # create a tensorboard logfile
    tboard = TensorBoard(log_dir=f"./logs/{name}")

    # adapt the learning rate
    lrs = LearningRateScheduler(schedule=update_lr, verbose=1)

    # save the good models
    mc = ModelCheckpoint(filepath=f"{name}.hdf", monitor="val_loss", verbose=1, save_best_only=True)

    ea = EarlyStopping(monitor="val_loss", mode="min", min_delta=0.5)

    return [tboard, lrs, mc, ea]


def convert_to_YCrCb(image):
    return cv2.cvtColor(image, cv2.COLOR_RGB2YCR_CB)


def main():
    lr_input_shape = (42, 42, 3)

    train_data, val_data, train_samp, val_samp = create_data_generator(path_lr="../../DSIDS/LR/tiles/4x_cubic/",
                                                                       path_hr="../../DSIDS/HR/tiles/",
                                                                       target_size_lr=(42, 42),
                                                                       target_size_hr=(168, 168),
                                                                       preproc_lr=None,  # convert_to_YCrCb,
                                                                       preproc_hr=None,  # convert_to_YCrCb,
                                                                       batch_size=16)
    # define the model type
    model_type = DENSE_TYPE_ALL

    # create a unique name for this trial
    name = f"{time.time()}-SRDense-Type-{model_type}"

    # create the model
    dense_model_net = dense_model(dense_type=model_type,
                                  input_shape=lr_input_shape,
                                  blocks=[8, 8, 8, 8, 8, 8, 8, 8],
                                  growth_rate=16)
    dense_model_net.name = name

    # save the summary of the model
    with open(f"SRDense-Type-{model_type}-summary.txt", 'w') as f:
        with redirect_stdout(f):
            dense_model_net.summary()
            print(f"Memory usage of model {get_model_memory_usage(16, dense_model_net)}")

    # print size of the model
    print(f"Memory usage of model {get_model_memory_usage(16, dense_model_net)}")

    # save some plots about the model
    plot_model(dense_model_net, f"SRDense-Type-{model_type}-plot.pdf")
    plot_model(dense_model_net, f"SRDense-Type-{model_type}-plot-shapes.pdf", show_shapes=True)

    # create the call backs
    callbacks = create_callbacks(name)

    # train the model
    #dense_model_net.fit(x_train, y_train, epochs=50, shuffle=True, validation_split=0.1, callbacks=callbacks)
    dense_model_net.fit_generator(train_data, steps_per_epoch=train_samp//16, epochs=50, validation_data=val_data, validation_steps=val_samp//16, shuffle=True)


if __name__ == "__main__":
    main()
