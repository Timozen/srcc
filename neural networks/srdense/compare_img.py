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


def main():
    path_to_file = "tim_animal_0001_0072.jpg"
    path_lr = "..\\..\\DSIDS\\LR\\tiles\\4x_cubic\\ignore"
    path_hr = "..\\..\\DSIDS\\HR\\tiles\\ignore"
    path_sr = "pred"

    img_lr = stitch_images(load_images("tim_animal_0001_", path_lr), 42, 42, 23, 17)
    img_hr = stitch_images(load_images("tim_animal_0001_", path_hr), 42*4, 42*4, 23, 17)
    img_sr = stitch_images(load_images("pred_", path_sr), 42*4, 42*4, 23, 17)
    fig = plt.figure("Compare", figsize=(16, 8))

    #ax1 = fig.add_subplot(1, 3, 1)
    ax2 = fig.add_subplot(1, 2, 1)
    ax3 = fig.add_subplot(1, 2, 2)
    #ax4 = fig.add_subplot(1, 4, 4)

    # ax1.imshow(cv2.cvtColor(img_lr, cv2.COLOR_BGR2RGB))
    # ax1.title.set_text("LR Image")
    ax2.imshow(cv2.cvtColor(img_hr, cv2.COLOR_BGR2RGB))
    ax2.title.set_text("HR Image")
    ax3.imshow(cv2.cvtColor(img_sr, cv2.COLOR_BGR2RGB))
    ax3.title.set_text("SR Image")

    img_lr_upscale = cv2.resize(img_lr, dsize=(42*4*23, 42*4*17), interpolation = cv2.INTER_CUBIC)

    cv2.imwrite("img_lr.jpg", img_lr)
    cv2.imwrite("img_lr_upscale.jpg", img_lr_upscale)
    cv2.imwrite("img_hr.jpg", img_hr)
    cv2.imwrite("img_sr.jpg", img_sr)

    # diff = np.abs(np.dot(img_sr[...,:3], [0.2989, 0.5870, 0.1140]) - np.dot(img_hr[...,:3], [0.2989, 0.5870, 0.1140]))
    # ax4.imshow(diff, cmap="gray")
    # ax4.title.set_text("Difference")
    plt.show()




if __name__ == "__main__":
    main()
