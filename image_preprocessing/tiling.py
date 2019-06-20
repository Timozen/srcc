import os
import sys
import cv2
import numpy as np

from tqdm import tqdm

'''
Example usage: 

    srcc/image_preprocessing$ python tiling.py -d DSIDS/HR

tiles all the images in the -d directory into the standard size 336x336.
'''

TILE_SHAPE = (336,336)

def parse_command_line_args():
    """A function for passing command line arguments.

    It searches for a source directory"""
    path = False

    # parsing the comment line arguments
    for arg in sys.argv[1:]:
        if arg == '-dir' or arg == '-d':
            path = True
        elif path == True:
            path = arg

    print('directory: ', path)

    # checking if path is given
    if type(path) is bool:
        print('no path given!')
        exit()

    # check if the path is a correct path
    if not os.path.isdir(path):
        print('invalid source directory: ', path)
        exit()

    return path



def main():
    """Main function for the rotating and clipping.

    parses command line arguments, builds file dictionary 
    and than copy + rename the files."""
    # change into the srcc directory
    os.chdir('..')
    # parsing the command line arguments
    src = parse_command_line_args()
    # path to the destination folder
    dst = os.path.join(os.getcwd(), 'DSIDS', 'HR', 'tiles_'+str(TILE_SHAPE[0]), 'ignore')

    # create the destination directory if necessary
    if not os.path.isdir(dst):
        os.makedirs(dst)

    # builds a list with all file names
    img_list = os.listdir(src)

    # iterate over all files
    for img in tqdm(img_list):
        if '.jpg' in img:
            # load image
            hr = cv2.imread(os.path.join(src, img), cv2.IMREAD_COLOR)
            
            num = 0
            #iterate over tiles in the image
            for i in range(0, hr.shape[0], TILE_SHAPE[0]):
                for j in range(0, hr.shape[1], TILE_SHAPE[1]):
                    if i + TILE_SHAPE[0] <= hr.shape[0] and j + TILE_SHAPE[1] <= hr.shape[1]:
                        # save the tile
                        cv2.imwrite(os.path.join(dst, img.split('.')[0]+ '_' + format(num, '04d') + '.jpg'), hr[i:i + TILE_SHAPE[0], j:j + TILE_SHAPE[1],:])
                        num += 1

    print('finished tiling!')


if __name__ == '__main__':
    main()
