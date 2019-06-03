import os
import sys
import cv2
import numpy as np

'''
Example usage: 

    srcc/image_preprocessing$ python rotate_and_clip.py -d DSIDS/HR

rotates all images in the -d directory and clips too big images into the standard size.
'''


def parse_command_line_args():
    """A function for passing command line arguments.

    It searches for a source and destination directory 
    as well as the author name."""
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


def build_file_dict(path):
    """Building a dictionary with all files in the source directory.

    src -- path to the source directory"""
    # create empty dictionary for all the files in src
    file_dict = {}
    # create an os walk through the src directory
    w = os.walk(path)

    # filling the dictionary with all files and parent paths in the source directory
    for root, folder, files in w:
        for f in files:
            if root in file_dict:  # checks if this subfolder is already in the file_dict
                file_dict[root].append(f)
            else:  # if not than create a list for the files in this subfolder
                file_dict[root] = [f]

    return file_dict


def main():
    """Main function for the rotating and clipping.

    parses command line arguments, builds file dictionary 
    and than copy + rename the files."""
    # change into the srcc directory
    os.chdir('..')
    # parsing the command line arguments
    path = parse_command_line_args()
    # building the file dictionary
    file_dict = build_file_dict(path)

    # iterating through the root folders
    for root in file_dict:
        # iterate through all the files in the root directory
        for f in file_dict[root]:
            # check if the file is a JPEG
            if '.jpg' in f:
                # read the image
                img = cv2.imread(os.path.join(root, f), cv2.IMREAD_COLOR)

                # check if its wrongly rotated
                if(img.shape[0] > img.shape[1]):
                    img = np.rot90(img)

                # check if it has too many rows
                if(img.shape[0] > 3024):
                    # calculate the difference
                    diff = img.shape[0] - 3024
                    # check if the difference is even
                    if diff % 2 == 0:
                        # crop the image
                        img = img[int(diff/2):-int(diff/2), :, :]
                    else:
                        # crop the image
                        img = img[int(diff/2)+1:-int(diff/2), :, :]

                # check if it has too many columns
                if(img.shape[1] > 4032):
                    # calculate the difference
                    diff = img.shape[1] - 4032
                    if diff % 2 == 0:
                        # crop the image
                        img = img[:, int(diff/2):-int(diff/2), :]
                    else:
                        # crop the image
                        img = img[:, int(diff/2)+1:-int(diff/2), :]

                # save the processed image
                cv2.imwrite(os.path.join(root, f), img)


if __name__ == '__main__':
    main()
