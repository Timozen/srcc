import os
import sys

'''
Example usage: 

    srcc/image_preprocessing$ python naming_structure.py -s raw_images -d DSIDS/HR -n niklas
With raw_images/landscape, raw_images/animal, ... as the categories.

It will create a folder DSIDS inside the srcc directory
with files like niklas_animal_0003 or niklas_landscape_0037 and so on.
'''


def parse_command_line_args():
    """A function for passing command line arguments.

    It searches for a source and destination directory 
    as well as the author name."""
    dst = False
    src = False
    author = False

    # parsing the comment line arguments
    for arg in sys.argv[1:]:
        if arg == '-src' or arg == '-s':
            src = True
        elif src == True:
            src = arg
        elif arg == '-dst' or arg == '-d':
            dst = True
        elif dst == True:
            dst = arg
        elif arg == '-name' or arg == '-n':
            author = True
        elif author == True:
            author = arg

    print('source: ', src)
    print('destination: ', dst)
    print('author name: ', author)

    # checking if source and destination paths are given
    if type(dst) is bool or type(src) is bool:
        print('no path given!')
        exit()

    # check if the source path is a correct path
    if not os.path.isdir(src):
        print('invalid source directory: ', src)
        exit()

    # create destination directory if necessary
    if not os.path.isdir(dst):
        os.makedirs(dst)

    return src, dst, author


def build_file_dict(src):
    """Building a dictionary with all files in the source directory.

    src -- path to the source directory"""
    # create empty dictionary for all the files in src
    file_dict = {}
    # create an os walk through the src directory
    w = os.walk(src)

    # filling the dictionary with all files and parent paths in the source directory
    for root, folder, files in w:
        for f in files:
            if root in file_dict:  # checks if this subfolder is already in the file_dict
                file_dict[root].append(f)
            else:  # if not than create a list for the files in this subfolder
                file_dict[root] = [f]

    return file_dict


def get_name(author, root, i):
    """Creates name string according to our convention"""
    # format the number with leading zeros
    number = format(i, "04d")
    # get the category from the root folder
    category = os.path.split(root)[1]

    # returns the finished name string
    return author+'_'+category+'_'+number+'.jpg'


def main():
    """Main function for the renaming script.

    parses command line arguments, builds file dictionary 
    and than copy + rename the files."""
    # change into the srcc directory
    os.chdir('..')
    # parsing the command line arguments
    src, dst, author = parse_command_line_args()
    # building the file dictionary
    file_dict = build_file_dict(src)

    # try to get highest number per category in DSIDS/HR/
    cat_i_dict = {}
    hr_flist = []
    # create path to HR images
    hr_path = os.path.join(os.getcwd(), 'DSIDS', 'HR')
    # check if the folder exists
    if os.path.isdir(hr_path):
        #create list with all hr files
        hr_flist = os.listdir(hr_path)

    # iterate over hr_flist
    for f in hr_flist:
        # split the file name
        name = f.split('_')
        # check for the right author
        if name[0] == author:
            if name[1] in cat_i_dict:
                cat_i_dict[name[1]] += 1
            else:
                cat_i_dict[name[1]] = 1

    # control output
    print('found the following number of images per category:')
    print(cat_i_dict)

    # iterating through the root folders
    for root in file_dict:
        # iterate through all the files in the root directory
        for i, f in enumerate(file_dict[root]):
            # check if the file is a JPEG
            if '.jpg' in f:
                # create category
                category = os.path.split(root)[1]
                # create category count
                cat_count = i
                # update cat_count if found
                if category in cat_i_dict:
                    cat_count += cat_i_dict[category]
                # create the path to the destination file
                tmp_dst_path = os.path.join(dst, get_name(author, root, cat_count))
                # create the path to the source file
                tmp_src_path = os.path.join(root, f)
                # rename + copy the file
                os.rename(tmp_src_path, tmp_dst_path)


if __name__ == '__main__':
    main()
