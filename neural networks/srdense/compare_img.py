import cv2
import os
import matplotlib.pyplot as plt
import numpy as np
import fnmatch

"""
This script will load an image from the dataset and print and save the resuls.
This will produce the stitched image and also one version where the 
boarders between the tiles are smoothed
"""

def load_image(file, path=None):
    # small hack if we only want to load one file manually!
    if path != None:
        return cv2.imread(os.path.join(path, file), cv2.IMREAD_COLOR)
    return cv2.imread(file, cv2.IMREAD_COLOR)


def load_images(file_name, path):
    images = []
    for file in os.listdir(path):
        # load only files fitting the name
        if fnmatch.fnmatch(file, file_name + "*.jpg"):
            images.append(load_image(file, path))
    return images


def stitch_images(images, width, height, x_dim, y_dim):
    # calculate the total size of the stitched image
    total_width = width * x_dim
    total_height = height * y_dim

    # create a new image
    stitched_image = np.zeros((total_height, total_width, 3), dtype=np.uint8)

    i, j = 0, 0
    # stitch the image by inserting the tiles one after another
    for image in images:
        stitched_image[j:(j+height), i:(i+width)] = image

        i += width
        if i == total_width:
            i = 0
            j += height

    return stitched_image


def remove_raster(image, width, height, x_dim, y_dim):
    """
    This function will try to smooth the boardes between tiles.
    really really simple approach. not fast! just for testing
    """
    # calculate the size of the image
    total_width = width * x_dim
    total_height = height * y_dim
    # create a mask
    mask = np.zeros((total_height, total_width), dtype=np.uint8)

    # create a copy because we want to keep the original
    ret = np.copy(image)

    # TODO this can be done smarter... if i had time ;(
    # mark the tile borders as 1
    x = width
    # go along the vertical borders
    while x < total_width:
        mask[:, (x-2):(x+1)] = 1
        x += width

    y = height
    # go along the horizontal bords
    while y < total_height:
        mask[(y-2):(y+1), :] = 1
        y += height

    # get all indexes
    idy, idx = np.nonzero(mask)

    # average all these points with the neighbours
    for x, y in zip(idx, idy):
        x_min = x-1 if x-1 >=0 else 0
        x_max = x+2 if x+2 <= total_width else total_width   
        y_min = y-1 if y-1 >=0 else 0
        y_max = y+2 if y+2 <=total_height else total_height

        if y_min == y_max:
            y_min -=1
        if x_min == x_max:
            x_min -=1

        # each color channel
        r = np.sum(image[y_min:y_max, x_min:x_max, 0]) / ((x_max-x_min) * (y_max - y_min)) 
        g = np.sum(image[y_min:y_max, x_min:x_max, 1]) / ((x_max-x_min) * (y_max - y_min)) 
        b = np.sum(image[y_min:y_max, x_min:x_max, 2]) / ((x_max-x_min) * (y_max - y_min))

        ret[y, x] = [r, g, b]     

    return ret.astype(np.uint8)


def main():
    # get path to dataset
    path_lr = "..\\..\\DSIDS\\LR\\tiles\\4x_cubic\\ignore"
    path_hr = "..\\..\\DSIDS\\HR\\tiles\\ignore"
    path_sr = "pred"

    # stich pack the original images
    img_lr = stitch_images(load_images("tim_animal_0001_", path_lr), 42, 42, 23, 17)
    img_hr = stitch_images(load_images("tim_animal_0001_", path_hr), 42*4, 42*4, 23, 17)
    # load the already predicted tiles... this could be included here too...
    img_sr = stitch_images(load_images("pred_", path_sr), 42*4, 42*4, 23, 17)

    img_sr_cleaned = remove_raster(img_sr,  42*4, 42*4, 23, 17)

    # plotting and saving
    fig = plt.figure("Compare", figsize=(16, 8))

    ax1 = fig.add_subplot(1, 3, 1)
    ax2 = fig.add_subplot(1, 3, 2)
    ax3 = fig.add_subplot(1, 3, 3)

    ax1.imshow(cv2.cvtColor(img_hr, cv2.COLOR_BGR2RGB))
    ax1.title.set_text("HR Image")
    ax2.imshow(cv2.cvtColor(img_sr, cv2.COLOR_BGR2RGB))
    ax2.title.set_text("SR Image")
    ax3.imshow(cv2.cvtColor(img_sr_cleaned, cv2.COLOR_BGR2RGB))
    ax3.title.set_text("SR Image Cleaned")

    img_lr_upscale = cv2.resize(img_lr, dsize=(42*4*23, 42*4*17), interpolation=cv2.INTER_CUBIC)

    cv2.imwrite("img_lr.jpg", img_lr)
    cv2.imwrite("img_lr_upscale.jpg", img_lr_upscale)
    cv2.imwrite("img_hr.jpg", img_hr)
    cv2.imwrite("img_sr.jpg", img_sr)
    cv2.imwrite("img_mask.jpg", img_sr_cleaned)

    plt.show()


if __name__ == "__main__":
    main()
