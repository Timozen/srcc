import time
from contextlib import redirect_stdout
from data_generator import create_data_generator
# for the callbacks
from keras.callbacks import EarlyStopping, LearningRateScheduler, TensorBoard
# testing stuff
from keras.datasets import mnist
from keras.layers import (AveragePooling2D, BatchNormalization, Conv2D,
                          Conv2DTranspose, Dense, Flatten, Input, ReLU,
                          concatenate)
from keras.losses import categorical_crossentropy
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


def dense_block(x, block_id, layers=3, filters=16, grow_filters=True, growth_rate=16, kernel_size=(3, 3), strides=(1, 1)):
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
        if growth_rate:
            filters += growth_rate

    return x, filters


def get_adam_optimizer(lr):
    return Adam(lr)

DENSE_TYPE_H = 1
DENSE_TYPE_HL = 2
DENSE_TYPE_ALL = 3

def dense_model(dense_type, input_shape, blocks=[3, 4, 5], filters=16, grow_filters=True, growth_rate=16, kernel_size=(3, 3), strides=(1, 1), weight_decay=1e-4):
    if len(blocks) < 2:
        return None

    inputs = Input(shape=input_shape, name="Input")

    res = Conv2D(filters=128,
                 kernel_size=kernel_size,
                 kernel_initializer='he_normal',
                 padding='same',
                 strides=strides,
                 use_bias=False,
                 kernel_regularizer=l2(weight_decay),
                 name="LowLevelFeatures")(inputs)
    res = ReLU(name="LowLevelFeatures-ReLU")(res)

    concat = []
    for i in range(len(blocks)):
        if i == 0:
            x, filters = dense_block(res, i, layers=blocks[i], filters=filters,
                                     grow_filters=grow_filters, growth_rate=growth_rate)
            if dense_type == DENSE_TYPE_ALL:
                concat = concatenate([res, x], axis=3, name=f"Block-OuterConcatenate-{i}")
            else:
                concat = x
        else:
            x, filters = dense_block(concat, i, layers=blocks[i], filters=filters,
                                     grow_filters=grow_filters, growth_rate=growth_rate)
            if dense_type == DENSE_TYPE_ALL:
                concat = concatenate([concat, x], axis=3, name=f"Block-OuterConcatenate-{i}")
            else:
                concat = x

    # add the bottle neck
    if dense_type == DENSE_TYPE_ALL:
        x = Conv2D(filters=256, kernel_size=(1, 1), strides=(1, 1), padding="same", use_bias=False, name="Bottleneck")(concat)
    elif dense_type == DENSE_TYPE_HL:
        x = concatenate([res, concat], axis=3, name=f"Block-OuterConcatenate-LH")
    
    # add the deconv. layers
    # deconv. layers are mostly referred as ConvTranspose
    x = Conv2DTranspose(filters=256, kernel_size=(3,3), strides=(2,2), padding="same", use_bias=False, name="Deconvolution-1")(x)
    x = ReLU(name="Deconvolution-1-ReLU")(x)
    x = Conv2DTranspose(filters=256, kernel_size=(3,3), strides=(2,2), padding="same", use_bias=False, name="Deconvolution-2")(x)
    x = ReLU(name="Deconvolution-2-ReLU")(x)

    # reconstruction layer
    outputs = Conv2D(filters=1, kernel_size=(3,3), strides=(1,1), padding="same", use_bias=False, name="Reconstruction")(x)
    
    model = Model(input=inputs, output=outputs)
    model.compile(loss=categorical_crossentropy,
                  optimizer=get_adam_optimizer(0.0001),
                  metrics=['accuracy'])
    return model


def update_lr(epoch, lr):
    if epoch == 30:
        lr /= 10
    return lr


def create_callbacks(name):
    tboard = TensorBoard(log_dir=f"./logs/{name}")
    lrs = LearningRateScheduler(schedule=update_lr, verbose=1)
    return [tboard, lrs]


def main():
    lr_input_shape = (84,84,1)    

    model_type = DENSE_TYPE_ALL

    name = f"{time.time()}-SRDense-Type-{model_type}"

    dense_model_net = dense_model(model_type, lr_input_shape, blocks=[8, 8, 8, 8, 8, 8, 8, 8], filters=16, growth_rate=16)
    dense_model_net.name = name

    with open(f"SRDense-Type-{model_type}-summary.txt", 'w') as f:
        with redirect_stdout(f):
            dense_model_net.summary()

    print(f"Memory usage of model {get_model_memory_usage(16, dense_model_net)}")
    plot_model(dense_model_net, f"SRDense-Type-{model_type}-plot.pdf")
    plot_model(dense_model_net, f"SRDense-Type-{model_type}-plot-shapes.pdf", show_shapes=True)

    callbacks = create_callbacks(name)

    #dense_model_net.fit(x_train, y_train, epochs=50, shuffle=True, validation_split=0.1, callbacks=callbacks)


if __name__ == "__main__":
    main()
