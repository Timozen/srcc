import cv2
import os
import matplotlib.pyplot as plt
import numpy as np

import compare_img


def main():
    n = 9
    k = 12

    images = compare_img.load_images("", "overlap")

    x = []
    y = []
    xy = []
    norm = []
    for i in range(n + (n-1)):
        for j in range(k + (k-1)):
            if i % 2 == 0 and not j % 2 == 0:
                print("x", i, j)
                x.append(images[i*(k+(k-1)) + j])
            elif not i % 2 == 0 and j % 2 == 0:
                print("y", i, j)
                y.append(images[i*(k+(k-1)) + j])
            elif i % 2 == 0 and j % 2 == 0:
                print("norm", i, j)
                norm.append(images[i*(k+(k-1)) + j])
            else:
                print("xy", i, j)
                xy.append(images[i*(k+(k-1)) + j])

    x_ = compare_img.stitch_images(x, 4032-336, 3024, 336, 336, k-1, n)
    y_ = compare_img.stitch_images(y, 4032, 3024-336, 336, 336, k, n-1)
    xy_ = compare_img.stitch_images(xy, 4032-336, 3024-336, 336, 336, k-1, n-1)
    img = compare_img.stitch_images(norm, 4032, 3024, 336, 336, k, n)

    cv2.imwrite("x.jpg", x_)
    cv2.imwrite("y.jpg", y_)
    cv2.imwrite("xy.jpg", xy_)
    cv2.imwrite("img.jpg", img)




if __name__ == "__main__":
    main()