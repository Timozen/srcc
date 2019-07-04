import cv2
import os
import matplotlib.pyplot as plt
import numpy as np
import fnmatch


def load_image(file, path=None):
    if path != None:
        return cv2.imread(os.path.join(path, file), cv2.IMREAD_COLOR)
    return cv2.imread(file, cv2.IMREAD_COLOR)


def load_images(file_name, path):
    images = []
    for file in os.listdir(path):
        if fnmatch.fnmatch(file, file_name + "*.jpg"):
            images.append(load_image(file, path))
    return images


def stitch_images(images, width, height, x_dim, y_dim):
    total_width = width * x_dim
    total_height = height * y_dim

    stitched_image = np.zeros((total_height, total_width, 3), dtype=np.uint8)

    i, j = 0, 0
    for image in images:
        stitched_image[j:(j+height), i:(i+width)] = image

        i += width
        if i == total_width:
            i = 0
            j += height

    return stitched_image


def remove_raster(image, width, height, x_dim, y_dim):
    total_width = width * x_dim
    total_height = height * y_dim
    mask = np.zeros((total_height, total_width), dtype=np.uint8)

    ret = np.copy(image)

    # TODO this can be done smarter...
    # mark the tile borders as 1
    x = width
    while x < total_width:
        mask[:, (x-2):(x+1)] = 1
        x += width

    y = height
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
    path_lr = "..\\..\\DSIDS\\LR\\tiles\\4x_cubic\\ignore"
    path_hr = "..\\..\\DSIDS\\HR\\tiles\\ignore"
    path_sr = "pred"

    img_lr = stitch_images(load_images("tim_animal_0001_", path_lr), 42, 42, 23, 17)
    img_hr = stitch_images(load_images("tim_animal_0001_", path_hr), 42*4, 42*4, 23, 17)
    img_sr = stitch_images(load_images("pred_", path_sr), 42*4, 42*4, 23, 17)

    img_sr_cleaned = remove_raster(img_sr,  42*4, 42*4, 23, 17)

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

    # diff = np.abs(np.dot(img_sr[...,:3], [0.2989, 0.5870, 0.1140]) - np.dot(img_hr[...,:3], [0.2989, 0.5870, 0.1140]))
    # ax4.imshow(diff, cmap="gray")
    # ax4.title.set_text("Difference")
    plt.show()


if __name__ == "__main__":
    main()
