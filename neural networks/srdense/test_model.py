from keras.models import load_model
import cv2
import os
import matplotlib.pyplot as plt
import numpy as np

import compare_img

"""
This script will predict the outputs of the srdense model for 
a given hardcoded image
"""

def main():
    path_to_file = "tim_animal_0001_"
    path_lr = "..\\..\\DSIDS\\LR\\tiles\\4x_cubic\\ignore"

    # load images
    images = compare_img.load_images(path_to_file, path_lr)
    # load the model
    model = load_model("models//srdense_epoch_80.h5")

    # propagate each image through the net alone, batch didnt work...
    for i, image in enumerate(images):
        # add one dimenion to the image
        x = np.expand_dims(image, axis=0)
        # predict the image
        pred = model.predict(x=x, batch_size=1, verbose=1)
        # remove the added extra dimension again :)
        pred = np.squeeze(pred, axis=0).astype(np.uint8)
        # save the image
        cv2.imwrite(f"pred/pred_{i:03d}.jpg", pred)


if __name__ == "__main__":
    main()
