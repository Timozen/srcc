import os
import numpy as np 
import cv2
import fnmatch
import Utils
from keras.models import load_model
import math
import tensorflow as tf
from tqdm import tqdm


def load_image(file, path=None):
    if path != None:
        return cv2.imread(os.path.join(path, file), cv2.IMREAD_COLOR)
    return cv2.imread(file, cv2.IMREAD_COLOR)


def load_images(file_name, path):
    images = []
    for file in sorted(os.listdir(path)):
        if fnmatch.fnmatch(file, file_name + "*.jpg"):
            images.append(load_image(file, path))
    return images


def calc_border_average(image_tile, border_size = 20):
    '''
    Calculates the border averages of a given tile

    image_tile -- numpy array with image tile
    border_size -- border size in pixel 

    returns [border_avg_up, border_avg_down, border_avg_left, border_avg_right]
            with list elements being RGB arrays
    '''
    '''
    print(np.mean(image_tile[0:border_size,:,:],axis=(0,1)))
    print(np.mean(image_tile[:,0:border_size,:],axis=(0,1)))
    print(np.mean(image_tile[-border_size-1:,:,:],axis=(0,1)))
    print(np.mean(image_tile[:,-border_size-1,:],axis=(0,1)))
    '''
    return [np.mean(image_tile[0:border_size,:,:],axis=(0,1)),      #Upper border
            np.mean(image_tile[-border_size-1:,:,:],axis=(0,1)),    #Down border
            np.mean(image_tile[:,0:border_size,:],axis=(0,1)),      #Left border
            np.mean(image_tile[:,-border_size-1:,:],axis=(0,1))]     #Right border

   

def calc_border_factors(image_tiles, rows=3024//336, cols=4032//336):

    factors = []

    def apply_factors(tile):
        #print(tile.shape)
        float_tile = tile.astype(np.float64)
        for y,x in np.ndindex(this.shape[0:2]):
            if x == 0 and y == 0:
                factor1_ = 0.5
                factor2_ = 0.5
            else:
                factor1_ = x/(x+y)
                factor2_ = y/(x+y)
            #print("x = ",x)
            #print("y = ",y)
            #print(factors)
            float_tile[y,x,:] = np.multiply(float_tile[y,x,:], factor1_ * factors[-1][1]) + np.multiply(float_tile[y,x,:], factor2_ * factors[-1][0])
        float_tile[float_tile > 255] = 255
    

        return float_tile.astype(np.uint8)

    
    for i in tqdm(range(rows + cols)):
        for k in tqdm(range(i)):
            if (i-k)>= 0 and (i-k) < rows and k >= 0 and k < cols :
                this = image_tiles[(i-k)*cols + k]
                if i == 0:
                    factors.append([[1,1,1],[1,1,1]]) 
                else:
                    if k == 0:
                        neighbor = image_tiles[(i-k-1)*cols + k]
                        factor = np.divide(calc_border_average(neighbor)[1],calc_border_average(this)[0])
                        factors.append([factor,factor])
                    elif k == i:
                        neighbor = image_tiles[(i-k)*cols + k-1]
                        factor = np.divide(calc_border_average(neighbor)[3],calc_border_average(this)[2])
                        factors.append([factor,factor])
                    else:
                        neighborLeft = image_tiles[(i-k)*cols + k-1]
                        neighborUp = image_tiles[(i-k-1)*cols + k]
                        factor1 = np.divide(calc_border_average(neighborLeft)[3],calc_border_average(this)[2])
                        factor2 = np.divide(calc_border_average(neighborUp)[1],calc_border_average(this)[0])
                        factors.append([factor1,factor2])
                    image_tiles[(i-k)*cols + k] = apply_factors(this)
    
    return image_tiles

def stitch_images(images,total_width, total_height, width, height, x_dim, y_dim):
    #total_width = width * x_dim
    #total_height = height * y_dim

    stitched_image = np.zeros((total_height, total_width, 3), dtype=np.uint8)

    i, j = 0, 0
    for image in images:
        stitched_image[j:(j+height), i:(i+width)] = image

        i += width
        if i == total_width:
            i = 0
            j += height

    return stitched_image

def overlap_images(img, img_x, img_y, img_xy, tile_dim_x, tile_dim_y):
    x_offset = tile_dim_x//2
    y_offset = tile_dim_y//2
    output = img

    def overlap_x_dir(input_img):
        output = input_img
        print("Overlapping x-direction")
        for col in tqdm(range(x_offset, input_img.shape[1]-x_offset)):
            x = col - x_offset                                  # x ist der Wert für die Sinusinterpolation und startet bei 0
            proportion = math.sin(math.pi/tile_dim_x * x)
            output[:,col,0:3] = input_img[:,col,0:3] * (1-proportion) + img_x[:,col-x_offset,0:3] * proportion
        return output
    def overlap_y_dir(input_img):
        output = input_img
        print("Overlapping y-direction")
        for row in tqdm(range(y_offset, input_img.shape[0]-y_offset)):
            y = row - y_offset                                  # y ist der Wert für die Sinusinterpolation und startet bei 0
            proportion = math.sin(math.pi/tile_dim_y * y)
            output[row,:,0:3] = input_img[row,:,0:3] * (1-proportion) + img_y[row-y_offset,:,0:3] * proportion
        return output
    def overlap_xy_dir(input_img):
        output = input_img
        print("Overlapping xy-direction")
        for row in tqdm(range(y_offset, input_img.shape[0]-y_offset)):
            y = row - y_offset                                     # x ist der Wert für die Sinusinterpolation und startet bei 0
            for col in range(x_offset, input_img.shape[1]-x_offset):
                x = col - x_offset                                 # y ist der Wert für die Sinusinterpolation und startet bei 0
                proportion = math.sin(math.pi/tile_dim_y * y) * math.sin(math.pi/tile_dim_x * x)
                output[row,col,0:3] = input_img[row,col,0:3] * (1-proportion) + img_y[row-y_offset,col-x_offset,0:3] * proportion
        return output
    
    output = overlap_x_dir(output)
    output = overlap_y_dir(output)
    output = overlap_xy_dir(output)
    return output


def stitching(image_tiles, LR = None, border_size=20, image_size=(3024,4032), LROffset = (0,0), overlap = False): 
    

    if LR is None: 
        corrected_image_tiles = calc_border_factors(image_tiles,                                # Uses the Stitching-Alg to correct all single tiles of the sr image tile list
                                                image_size[0]//image_tiles[0].shape[0],
                                                image_size[1]//image_tiles[0].shape[1])     
        output = stitch_images(corrected_image_tiles,                                           # simply stitches the corrected tiles into one image
                            image_size[1], image_size[0],
                            image_tiles[0].shape[0],
                            image_tiles[0].shape[1], 
                            image_size[1]//image_tiles[0].shape[1], 
                            image_size[0]//image_tiles[0].shape[0])
        return output
    
    if overlap is True:
        
        img_list, img_x_shifted_list, img_y_shifted_list, img_xy_shifted_list = get_shifted_images(image_tiles,image_size[1],image_size[0],image_tiles[0].shape[1],image_tiles[0].shape[0])
        print("Calculating x_norm image")
        img_ = stitching(img_list,LR,border_size,image_size, overlap = False)
        print("Calculating x_shifted image")
        img_x_shifted_ = stitching(img_x_shifted_list,LR,border_size, image_size=(image_size[0] , image_size[1]-image_tiles[0].shape[1] ),                              # gets the x shifted hsv corrected advance stitch image
                                                                      LROffset= (0,image_tiles[0].shape[1]//2), overlap=False)
        print("Calculating y_shifted image")
        img_y_shifted_ = stitching(img_y_shifted_list,LR,border_size, image_size=(image_size[0]-image_tiles[0].shape[0] , image_size[1] ),                              # gets the y shifted hsv corrected advance stitch image
                                                                      LROffset= (image_tiles[0].shape[0]//2,0), overlap=False)
        print("Calculating xy_shifted image")
        img_xy_shifted_ = stitching(img_xy_shifted_list,LR,border_size, image_size=(image_size[0]-image_tiles[0].shape[0] , image_size[1]-image_tiles[0].shape[1] ),    # gets the xy shifted hsv corrected advance stitch image
                                                                        LROffset= (image_tiles[0].shape[0]//2,image_tiles[0].shape[1]//2), overlap=False)
        return overlap_images(img_, img_x_shifted_, img_y_shifted_, img_xy_shifted_, image_tiles[0].shape[1], image_tiles[0].shape[0]) 


    # this code runs if LR==None and overlap==False (Advanced stitching without overlapping tiles)
    print("Calculating interpolations and hsv")
    corrected_image_tiles = calc_border_factors(image_tiles,                                # Uses the Stitching-Alg to correct all single tiles of the sr image tile list
                                                image_size[0]//image_tiles[0].shape[0],
                                                image_size[1]//image_tiles[0].shape[1])     
    output = stitch_images(corrected_image_tiles,                                           # simply stitches the corrected tiles into one image
                            image_size[1], image_size[0],
                            image_tiles[0].shape[0],
                            image_tiles[0].shape[1], 
                            image_size[1]//image_tiles[0].shape[1], 
                            image_size[0]//image_tiles[0].shape[0])

    HR_ = cv2.resize(LR, (0,0) , fx = 4, fy = 4, interpolation = cv2.INTER_CUBIC)           # Upscales the LR image
    hsv = cv2.cvtColor(HR_,cv2.COLOR_BGR2HSV)                                               # Converts the upscaled LR image into HSV
    outputHSV = cv2.cvtColor(output,cv2.COLOR_BGR2HSV)                                      # Converts the corrected SR image into HSV
    outputHSV[:,:,0:2] = hsv[LROffset[0]:outputHSV.shape[0]+LROffset[0],
                             LROffset[1]:outputHSV.shape[1]+LROffset[1],0:2]                # Replaces HS-values of the SR image by the LR image in the valid area
    output = cv2.cvtColor(outputHSV,cv2.COLOR_HSV2BGR)                                      # Converts the color-corrected SR image into BGR (RGB-Space)
    return output


def get_shifted_images(images, total_width, total_height, width, height):
    '''
    Seperates shifted images into a list of tiles of each shifted images.

    images -- list of all tiles
    total_width,total_height -- pixel size of the HR image
    width, height -- pixel size of a single tile

    returns lists of tiles in the following order: img, x-shifted, y-shifted, xy-shifted
    '''

    n = total_height // height
    k = total_width // width

    x = []
    y = []
    xy = []
    norm = []
    for i in range(n + (n-1)):
        for j in range(k + (k-1)):
            if i % 2 == 0 and not j % 2 == 0:

                x.append(images[i*(k+(k-1)) + j])
            elif not i % 2 == 0 and j % 2 == 0:
                y.append(images[i*(k+(k-1)) + j])
            elif i % 2 == 0 and j % 2 == 0:
                norm.append(images[i*(k+(k-1)) + j])
            else:
                xy.append(images[i*(k+(k-1)) + j])
    '''
    x_ = stitch_images(x, total_width-k, total_height, k, n, k-1, n)
    y_ = stitch_images(y, total_width, total_height-n, k, n, k, n-1)
    xy_ = stitch_images(xy, total_width-k, total_height-n, k, n, k-1, n-1)
    img = stitch_images(norm, total_width, total_height, k, n, k, n)
    '''
    return norm, x, y, xy


def main():
    lr = cv2.cvtColor(cv2.imread(os.path.join("ISTest","img_lr.jpg")), cv2.COLOR_BGR2RGB)
    model = load_model(os.path.join("models","initialized_gen_model20.h5"), custom_objects={"tf":tf})
    lr_tiles_overlap = Utils.tile_image(lr, shape=(126,126), overlap=True)
    sr_tiles = []
    for tile in tqdm(lr_tiles_overlap):
        sr_tiles.append(   Utils.denormalize(np.squeeze(model.predict(np.expand_dims(Utils.rescale_imgs_to_neg1_1(tile), axis=0)), axis=0))  )
    
    final_image = stitching(sr_tiles, lr, border_size=20, image_size=(3024,4032), LROffset = (0,0), overlap = True)
    cv2.imwrite(os.path.join("ISTest","image_final.jpg"), cv2.cvtColor(final_image, cv2.COLOR_RGB2BGR))



if __name__ == "__main__":
    main()