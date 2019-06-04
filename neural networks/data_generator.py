from keras.preprocessing.image import ImageDataGenerator
import matplotlib.pyplot as plt


def create_data_generator(path_lr, path_hr,
                          target_size_lr=(4032//4, 3024//4),
                          target_size_hr=(4032, 3024),
                          rescale=1./255,
                          horizontal_flip=True,
                          vertical_flip=True,
                          validation_split=0.10,
                          batch_size=32,
                          color_mode="rgb",
                          seed=1337):
    """ This function will create the Keras Image Data generator for the models.

    With this function we can create the same kind of training and validation
    style for different datasets.
    path_lr -- the path to the LR images CAVEAT all pictures need to in in a subfolder
    path_hr -- the path to the HR images CAVEAT all pictures need to be in a subfolder
    target_size_lr -- (width, height) tupel for picture size, autoscale if not fitting
    target_size_hr -- (width, height) tupel for picture size, autoscale if not fitting
    rescale -- multiply all the values with the factor - default 1./255 for normalizing
    horizontal_flip -- T/F flip the pictures horizontally for data augmentation
    vertical_flip -- T/F flip the pictures vertically for data augmentation
    batch_size -- size of one batch
    color_mode -- "rgb" or "grayscale" if the pictes shall have different colormap
    seed -- used for the inital same random value, so x and y are sorted same
    """

    # load all the arguments of the ImageDataGenerator as dictionary
    data_gen_args = dict(
        rescale=rescale,
        horizontal_flip=horizontal_flip,
        vertical_flip=vertical_flip,
        validation_split=validation_split
    )

    # create an ImageDataGenerator for LR and HR images with the !same! parameters
    lr_image_datagen = ImageDataGenerator(**data_gen_args)
    hr_image_datagen = ImageDataGenerator(**data_gen_args)

    # create the lr image training generator, use the same seed everywhere
    # caveat - use the target_size_lr
    lr_image_training_generator = lr_image_datagen.flow_from_directory(
        directory=path_lr,
        class_mode=None,
        target_size=target_size_lr,
        color_mode=color_mode,
        batch_size=batch_size,
        subset="training",
        seed=seed
    )
    # create the hr image training generator, use the same seed everywhere
    # caveat - use the target_size_hr
    hr_image_training_generator = hr_image_datagen.flow_from_directory(
        directory=path_hr,
        class_mode=None,
        target_size=target_size_hr,
        color_mode=color_mode,
        batch_size=batch_size,
        subset="training",
        seed=seed
    )
    # because class_mode=None makes only x return, our LR is x and HR is the y
    # so combine both generators as (x, y) with zip
    image_training_generator = zip(lr_image_training_generator, hr_image_training_generator)

    # Same as above, but only use subset="validation" for using the remaining
    # pictures
    lr_image_validation_generator = lr_image_datagen.flow_from_directory(
        directory=path_lr,
        class_mode=None,
        target_size=target_size_lr,
        color_mode=color_mode,
        batch_size=batch_size,
        subset="validation",
        seed=seed
    )
    hr_image_validation_generator = hr_image_datagen.flow_from_directory(
        directory=path_hr,
        class_mode=None,
        target_size=target_size_hr,
        color_mode=color_mode,
        batch_size=batch_size,
        subset="validation",
        seed=seed
    )
    # same reason as above
    image_validation_generator = zip(lr_image_validation_generator, hr_image_validation_generator)

    # return our training and validation generator
    return image_training_generator, image_validation_generator


def main():
    # load some test pictures
    # in test_lr and test_hr has to be one more subfolder containing all the
    # images... yeah thats a requirment of flow_from_directory...
    itg, _ = create_data_generator("../DSIDS/test_lr/", "../DSIDS/test_hr")

    # load one batch
    batch_itg = next(itg)

    # plot (x,y) of the first image to check if it is the same image with the
    # correct dimensions
    _, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 15))
    ax1.imshow(batch_itg[0][1])
    ax2.imshow(batch_itg[1][1])
    plt.show()


if __name__ == "__main__":
    main()
