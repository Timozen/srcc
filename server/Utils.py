import numpy as np 
import cv2
from tqdm import tqdm


def rescale_imgs(img, mi, ma):
    '''this function rescales a np.ndarray img to an interval [mi, ma]

    img -- np.ndarray with values in [0, 255]
    mi -- minimum of interval
    ma -- maximum of interval
    '''
    return np.interp(img, (0,255), (mi, ma))

def rescale_imgs_to_neg1_1(img):
    return rescale_imgs(img, -1, 1)

def denormalize(input_data):
    input_data = (input_data + 1) * 127.5
    input_data[input_data < 0] = 0
    input_data[input_data > 255] = 255
    return input_data.astype(np.uint8)


def tile_image(img, shape=(336,336), overlap=False):
    """function to crop one lr image into tiles for prediction.

    img -- the lr image as a numpy array
    shape -- the shape of the tiles
    overlap -- should the tiles overlap by half a tile?    
    """

    # calc the step size for the tiling according to overlap
    if overlap:
        # overlap -> half a tile per step
        step_size = (shape[0]//2, shape[1]//2)
    else:
        # no overlap -> a whole tile per step
        step_size = shape

    x_dim = img.shape[1]//shape[1]
    y_dim = img.shape[0]//shape[0]

    img = img[:y_dim*shape[0], :x_dim*shape[1], :]

    tile_list = []
    for i in range(0, img.shape[0], step_size[0]):
        for j in range(0, img.shape[1], step_size[1]):
            # test if this tile is part of the image
            if i + shape[0] <= img.shape[0] and j + shape[1] <= img.shape[1]:
                # append the new tile
                tile_list.append(img[i:i + shape[0], j:j + shape[1],:])
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

    