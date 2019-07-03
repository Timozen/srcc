import numpy as np 
import cv2
from tqdm import tqdm

def crop_lr_image(img, hr_shape=(336,336), overlap=False):
    """function to crop one lr image into tiles for prediction.

    img -- the lr image as a numpy array
    shape -- the shape of the tiles
    overlap -- should the tiles overlap by half a tile?    
    """
    # calc the lr tile shape
    lr_shape = (hr_shape[0]//4, hr_shape[1]//4)

    # calc the step size for the tiling according to overlap
    if overlap:
        # overlap -> half a tile per step
        step_size = (lr_shape[0]//2, lr_shape[1]//2)
    else:
        # no overlap -> a whole tile per step
        step_size = lr_shape

    tile_list = []
    for i in range(0, img.shape[0], step_size[0]):
        for j in range(0, img.shape[1], step_size[1]):
            # test if this tile is part of the image
            if i + lr_shape[0] <= img.shape[0] and j + lr_shape[1] <= img.shape[1]:
                # append the new tile
                tile_list.append(img[i:i + lr_shape[0], j:j + lr_shape[1],:])
    # return the list of tiles
    return tile_list


def crop_into_lr_shape(img, shape=(756, 1008)):
    """function to crop one slightly to big lr image into the correct shape.

    img -- the a little to big lr image as a numpy array
    shape -- the correct lr shape   
    """
    rot = False
    # check if its wrongly rotated
    if(img.shape[0] > img.shape[1]):
        img = np.rot90(img)
        rot = True

    # check if it has too many rows
    if(img.shape[0] > shape[0]):
        # calculate the difference
        diff = img.shape[0] - shape[0]
        # check if the difference is even
        if diff % 2 == 0:
            # crop the image
            img = img[int(diff/2):-int(diff/2), :, :]
        else:
            # crop the image
            img = img[int(diff/2)+1:-int(diff/2), :, :]

    # check if it has too many columns
    if(img.shape[1] > shape[1]):
        # calculate the difference
        diff = img.shape[1] - shape[1]
        # check if the difference is even
        if diff % 2 == 0:
            # crop the image
            img = img[:, int(diff/2):-int(diff/2), :]
        else:
            # crop the image
            img = img[:, int(diff/2)+1:-int(diff/2), :]

    # rotate back if it was rotated
    if rot:
        img = np.rot90(img, k=3)
    
    # return cropped image
    return img

    