import time
from contextlib import redirect_stdout

# for the callbacks
from keras.callbacks import EarlyStopping, TensorBoard
# testing stuff
from keras.datasets import mnist

from keras.layers import (AveragePooling2D, BatchNormalization, Conv2D, Dense,
                          Flatten, Input, ReLU, concatenate)
from keras.losses import categorical_crossentropy
from keras.models import Model
from keras.optimizers import SGD
from keras.regularizers import l2
from keras.utils import to_categorical
from keras.utils.vis_utils import plot_model

from memory_usage import get_model_memory_usage


def dense_layer(inputs, filters=12, kernel_size=(3, 3), strides=(1, 1)):
    """
    This function will build a single dense layer of a dense block.

    inputs -- the outputs from the prev. layer
    filters -- how many conv. layers will be created
    kernel_size -- size of the conv. kernel
    strides -- the stride size of the kernel, should be (1,1) in most cases
    """
    x = BatchNormalization()(inputs)
    x = ReLU()(x)
    x = Conv2D(filters=filters,
               strides=strides,
               kernel_size=kernel_size,
               padding='same',
               kernel_initializer='he_normal',
               use_bias=False)(x)
    return x


def dense_block(x, layers=3, filters=16, grow_filters=True, growth_rate=16, kernel_size=(3, 3), strides=(1, 1)):
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
        cb = dense_layer(x, filters=growth_rate)
        # connected the output from all prev. ones
        if i == 0:
            x = cb
        else:
            x = concatenate([x, cb], axis=3)
        if growth_rate:
            filters += growth_rate

    return x, filters


def transition_layer(inputs, filters=16, compression=1.0, weight_decay=1e-4):
    """
    This layer is between two dense blocks.
    """
    x = BatchNormalization()(inputs)
    x = ReLU()(x)
    x = Conv2D(filters=int(filters * compression),
               kernel_size=(1, 1),
               padding="same",
               use_bias=False,
               kernel_initializer='he_normal',
               kernel_regularizer=l2(weight_decay))(x)
    #x = AveragePooling2D((2, 2), strides=(2, 2))(x)
    return x



def dense_model(x_train, blocks=[3, 4, 5], filters=16, grow_filters=True, growth_rate=16, kernel_size=(3, 3), strides=(1, 1), weight_decay=1e-4):
    if len(blocks) < 2:
        return None

    inputs = Input(x_train.shape[1:])

    x = Conv2D(filters=filters,
               kernel_size=kernel_size,
               kernel_initializer='he_normal',
               padding='same',
               strides=strides,
               use_bias=False,
               kernel_regularizer=l2(weight_decay))(inputs)

    # create the dense blocks, but the last one
    # doesnt need a transition layer
    for i in range(len(blocks) - 1):
        x, filters = dense_block(x, layers=blocks[i], filters=filters, grow_filters=grow_filters, growth_rate=growth_rate)
        x = transition_layer(x, filters=filters)
    x, filters = dense_block(x, layers=blocks[-1], filters=filters, grow_filters=grow_filters, growth_rate=growth_rate)

    x = Flatten()(x)
    x = Dense(64, activation='softmax')(x)
    predictions = Dense(10, activation='softmax')(x)

    model = Model(input=inputs, output=predictions)
    model.compile(loss=categorical_crossentropy,
                  optimizer=SGD(lr=1e-2, decay=1e-6),
                  metrics=['accuracy'])
    return model

def create_callbacks(name):
    tboard = TensorBoard(log_dir=f"./logs/{name}")

    return [tboard]

def main():
    (x_train, y_train), (x_test, y_test) = mnist.load_data()

    x_train = x_train.reshape((60000, 28, 28, 1))[:5000]
    y_train = to_categorical(y_train, 10)[:5000]

    name = f"{time.time()}-SRDense"

    dense_model_net = dense_model(x_train, blocks=[8,8,8,8], filters=16, growth_rate=16)
    dense_model_net.name = name
    dense_model_net.summary()

    with open('srdense_summary.txt', 'w') as f:
        with redirect_stdout(f):
            dense_model_net.summary()

    print(f"Memory usage of model {get_model_memory_usage(32, dense_model_net)}")
    plot_model(dense_model_net, 'plot_srdense.pdf')
    plot_model(dense_model_net, 'plot_srdense_shapes.pdf', show_shapes=True)

    
    callbacks = create_callbacks(name)

    dense_model_net.fit(x_train, y_train, epochs=50, shuffle=True, validation_split=0.1, callbacks=callbacks)


if __name__ == "__main__":
    main()
