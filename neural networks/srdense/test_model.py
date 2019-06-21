from keras.models import load_model
import cv2
import os
import matplotlib.pyplot as plt
import numpy as np

import compare_img


def main():
    path_to_file = "tim_animal_0001_"
    path_lr = "..\\..\\DSIDS\\LR\\tiles\\4x_cubic\\ignore"

    images = compare_img.load_images(path_to_file, path_lr)
    model = load_model("models//1560500514.1100993-SRDense-Type-3.h5")

    for i, image in enumerate(images):
        x = np.expand_dims(image, axis=0)
        pred = model.predict(x=x, batch_size=1, verbose=1)
        pred = np.squeeze(pred, axis=0).astype(np.uint8)
        cv2.imwrite(f"pred/pred_{i:03d}.jpg", pred)

if __name__ == "__main__":
    main()
