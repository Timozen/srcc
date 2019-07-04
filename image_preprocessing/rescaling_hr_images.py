import os
import sys
import numpy as np
import cv2
from tqdm import tqdm

'''
Expects a folder DSIDS/HR and DSIDS/LR in the srcc directory.
'''
DOWN_SCALING_FACTOR = 4
# area vs cubic vs linear vs nearest vs lanczos
INTERPOLATION = 'cubic'

#shape = (504, 504) #tiles
shape = (3024,4032)


# using correct constant for the given INTERPOLATION
if INTERPOLATION == 'area':
    inter = cv2.INTER_AREA
elif INTERPOLATION == 'cubic':
    inter = cv2.INTER_CUBIC
elif INTERPOLATION == 'linear':
    inter = cv2.INTER_LINEAR
elif INTERPOLATION == 'lanczos':
    inter = cv2.INTER_LANCZOS4
elif INTERPOLATION == 'nearest':
    inter = cv2.INTER_NEAREST


def main():
    """Main function for the rescaling script.

    First builds a list with all HR images. 
    After that loads the images and scales them down.
    At last saves the images in the LR directory."""

    # change into the srcc directory
    os.chdir('..')
    
    ''' #tiles
    # path to the HR images
    src = os.path.join(os.getcwd(), 'DSIDS', 'HR', 'tiles_'+str(shape[0]), 'ignore')
    # path to the destination folder
    dst = os.path.join(os.getcwd(), 'DSIDS', 'LR', 'tiles_'+str(shape[0]),
                       str(DOWN_SCALING_FACTOR)+'x_'+INTERPOLATION, 'ignore')'''
    
    src = os.path.join(os.getcwd(), 'DSIDS', 'HR')
    dst = os.path.join(os.getcwd(), 'DSIDS', 'LR', str(DOWN_SCALING_FACTOR)+'x_'+INTERPOLATION)

    # create the destination directory if necessary
    if not os.path.isdir(dst):
        os.makedirs(dst)

    # builds a list with all file names
    img_list = os.listdir(src)

    # iterate over all files
    for i, img in tqdm(enumerate(img_list)):
        # load image
        hr = cv2.imread(os.path.join(src, img), cv2.IMREAD_COLOR)
        # resize the image with bicubic interpolation
        lr = cv2.resize(hr, (0, 0),
                        fx=1/DOWN_SCALING_FACTOR,
                        fy=1/DOWN_SCALING_FACTOR,
                        interpolation=inter)
        # save the resized image
        cv2.imwrite(os.path.join(dst, img), lr)

if __name__ == '__main__':
    main()
