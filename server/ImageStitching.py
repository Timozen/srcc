import os
import numpy as np 
import cv2
import fnmatch
import Utils
from keras.models import load_model
import math
import tensorflow as tf
from tqdm import tqdm


def load_image(file, path=None):    #Loads images
    if path != None:
        return cv2.imread(os.path.join(path, file), cv2.IMREAD_COLOR)
    return cv2.imread(file, cv2.IMREAD_COLOR)


def load_images(file_name, path):   #overloads load_image
    images = []
    for file in sorted(os.listdir(path)):
        if fnmatch.fnmatch(file, file_name + "*.jpg"):
            images.append(load_image(file, path))
    return images


def calc_border_average(image_tile, border_size = 20):              #Calculates the average of the borders of a singe tile
    return [np.mean(image_tile[0:border_size,:,:],axis=(0,1)),      #Upper border
            np.mean(image_tile[-border_size-1:,:,:],axis=(0,1)),    #Down border
            np.mean(image_tile[:,0:border_size,:],axis=(0,1)),      #Left border
            np.mean(image_tile[:,-border_size-1:,:],axis=(0,1))]     #Right border

   

def calc_border_factors(image_tiles, rows=3024//336, cols=4032//336):   #Calculates the weight factors between two tiles

    factors = []
    mask = np.zeros(image_tiles[0].shape).astype(np.float32)
    
    for y,x in np.ndindex(mask.shape[0:2]):
        if x == 0 and y == 0:
            factor1_ = 0.5
        else:
            factor1_ = x/(x+y)
            mask[y,x,0] = factor1_
            mask[y,x,1] = factor1_
            mask[y,x,2] = factor1_
    

    def apply_factors(tile):                    #Applies the factors for a tile to match neighboured gray scale factors
        float_tile = tile.astype(np.float64)

        float_tile[:,:,0] = np.multiply(float_tile[:,:,0], mask[:,:,0] * factors[-1][1][0]) + np.multiply(float_tile[:,:,0], np.subtract(np.ones(mask.shape), mask[:,:,:])[:,:,0] *factors[-1][0][0])
        float_tile[:,:,1] = np.multiply(float_tile[:,:,1], mask[:,:,1] * factors[-1][1][1]) + np.multiply(float_tile[:,:,1], np.subtract(np.ones(mask.shape), mask[:,:,:])[:,:,1] *factors[-1][0][1])
        float_tile[:,:,2] = np.multiply(float_tile[:,:,2], mask[:,:,2] * factors[-1][1][2]) + np.multiply(float_tile[:,:,2], np.subtract(np.ones(mask.shape), mask[:,:,:])[:,:,2] *factors[-1][0][2])
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

def stitch_images(images,total_width, total_height, width, height, x_dim, y_dim): #puts tiles together
    stitched_image = np.zeros((total_height, total_width, 3), dtype=np.uint8)
    i, j = 0, 0
    for image in images:
        stitched_image[j:(j+height), i:(i+width)] = image
        i += width
        if i == total_width:
            i = 0
            j += height

    return stitched_image

def overlap_images(img, img_x, img_y, img_xy, tile_dim_x, tile_dim_y): #Uses masks to optimize interpolation between image overlaps
    x_offset = tile_dim_x//2
    y_offset = tile_dim_y//2
    output = img

    def f(x,y): #The four proportion functions
        return ((-np.cos((2*math.pi)/tile_dim_x * x )+1)*(-np.cos((2*math.pi)/tile_dim_y * y)+1))/4
    def f_y(x,y):
        return ((-np.cos((2*math.pi)/tile_dim_x * x +math.pi)+1)*(-np.cos((2*math.pi)/tile_dim_y * y)+1))/4
    def f_x(x,y):
        return ((-np.cos((2*math.pi)/tile_dim_x * x )+1)*(-np.cos((2*math.pi)/tile_dim_y * y+math.pi)+1))/4
    def f_xy(x,y):
        return ((-np.cos((2*math.pi)/tile_dim_x * x +math.pi)+1)*(-np.cos((2*math.pi)/tile_dim_y * y+math.pi)+1))/4

    #Declaring the masks
    norm_weights = np.fromfunction(lambda i, j:  f(i,j), (img.shape[0], img.shape[1]), dtype=np.float32)
    norm_weights = np.stack((norm_weights,norm_weights,norm_weights),axis=2)

    x_weights = np.fromfunction(lambda i, j:  f_x(i,j-y_offset), (img.shape[0], img.shape[1]-tile_dim_x), dtype=np.float32)
    x_weights = np.stack((x_weights,x_weights,x_weights),axis=2)

    y_weights = np.fromfunction(lambda i, j:  f_y(i-x_offset,j), (img.shape[0]-tile_dim_y, img.shape[1]), dtype=np.float32)
    y_weights = np.stack((y_weights,y_weights,y_weights),axis=2)

    xy_weights = np.fromfunction(lambda i, j:  f_xy(i-x_offset,j-y_offset), (img.shape[0]-tile_dim_y, img.shape[1]-tile_dim_x), dtype=np.float32)
    xy_weights = np.stack((xy_weights,xy_weights,xy_weights),axis=2)

    x_border_weights = np.fromfunction(lambda i, j:  (-np.cos((2*math.pi)/tile_dim_y * (i- y_offset)+math.pi)+1) / 2, (img.shape[0]-tile_dim_y, x_offset), dtype=np.float32)
    x_border_weights = np.stack((x_border_weights,x_border_weights,x_border_weights),axis=2)

    y_border_weights = np.fromfunction(lambda i, j:  (-np.cos((2*math.pi)/tile_dim_x * (j- x_offset)+math.pi)+1) / 2, (y_offset, img.shape[1]-tile_dim_x), dtype=np.float32)
    y_border_weights = np.stack((y_border_weights,y_border_weights,y_border_weights),axis=2)

    def overlap(input_img): #Applying the masks on the images
        output = input_img
        print("Overlapping ")
        output[y_offset:input_img.shape[0]-y_offset, x_offset:input_img.shape[1]-x_offset, 0:3] = \
                                    np.multiply(input_img[y_offset:input_img.shape[0]-y_offset,x_offset:input_img.shape[1]-x_offset,0:3], norm_weights[y_offset:input_img.shape[0]-y_offset,x_offset:input_img.shape[1]-x_offset,0:3]) + \
                                    np.multiply(img_x[y_offset:input_img.shape[0]-y_offset,:,0:3],x_weights[y_offset:input_img.shape[0]-y_offset,:,0:3]) + \
                                    np.multiply(img_y[:,x_offset:input_img.shape[1]-x_offset,0:3],y_weights[:,x_offset:input_img.shape[1]-x_offset,0:3]) + \
                                    np.multiply(img_xy[:,:,0:3],xy_weights[:,:,0:3])
        output[y_offset:input_img.shape[0]-y_offset,0:x_offset, 0:3] = \
                                    np.multiply(img_y[:,0:x_offset,0:3],x_border_weights[:,:,0:3]) + \
                                    np.multiply(input_img[y_offset:input_img.shape[0]-y_offset,0:x_offset, 0:3],np.subtract(np.ones(x_border_weights.shape),x_border_weights[:,:,0:3]))
        output[y_offset:input_img.shape[0]-y_offset,input_img.shape[1]-x_offset:, 0:3] = \
                                    np.multiply(img_y[:,input_img.shape[1]-x_offset:,0:3],x_border_weights[:,:,0:3]) + \
                                    np.multiply(input_img[y_offset:input_img.shape[0]-y_offset,input_img.shape[1]-x_offset:, 0:3],np.subtract(np.ones(x_border_weights.shape),x_border_weights[:,:,0:3]))
        output[0:y_offset,x_offset:input_img.shape[1]-x_offset, 0:3] = \
                                    np.multiply(img_x[0:y_offset,:,0:3],y_border_weights[:,:,0:3]) + \
                                    np.multiply(input_img[0:y_offset,x_offset:input_img.shape[1]-x_offset, 0:3],np.subtract(np.ones(y_border_weights.shape),y_border_weights[:,:,0:3]))
        output[input_img.shape[0]-y_offset:,x_offset:input_img.shape[1]-x_offset, 0:3] = \
                                    np.multiply(img_x[input_img.shape[0]-y_offset:,:,0:3],y_border_weights[:,:,0:3]) + \
                                    np.multiply(input_img[input_img.shape[0]-y_offset:,x_offset:input_img.shape[1]-x_offset, 0:3],np.subtract(np.ones(y_border_weights.shape),y_border_weights[:,:,0:3]))
        return output
    
    output = overlap(output)
    return output


def stitching(image_tiles, LR = None, border_size=20, image_size=(3024,4032), LROffset = (0,0), overlap = True, adjustRGB = True): #The main algorithm
    
    if adjustRGB == False:
        LR = None

    if (LR is None) and overlap==False: #just rgb adjusting, other cases are intercepted in the server call
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
    
    if overlap is True:       #overlapping is on
        if adjustRGB == True : #a non recommanded path with due to unstable RGB adjusting
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
        #overlapping as the normal case (recommended)
        img_list, img_x_shifted_list, img_y_shifted_list, img_xy_shifted_list = get_shifted_images(image_tiles,image_size[1],image_size[0],image_tiles[0].shape[1],image_tiles[0].shape[0])
        img_ = stitch_images(img_list,image_size[1],image_size[0],image_tiles[0].shape[1],image_tiles[0].shape[0],image_size[1]//image_tiles[0].shape[1], image_size[0]//image_tiles[0].shape[0])
        img_x_shifted_ = stitch_images(img_x_shifted_list,image_size[1]-image_tiles[0].shape[1],image_size[0],image_tiles[0].shape[1],image_tiles[0].shape[0],image_size[1]//image_tiles[0].shape[1], image_size[0]//image_tiles[0].shape[0])
        img_y_shifted_ = stitch_images(img_y_shifted_list,image_size[1],image_size[0]-image_tiles[0].shape[0],image_tiles[0].shape[1],image_tiles[0].shape[0],image_size[1]//image_tiles[0].shape[1], image_size[0]//image_tiles[0].shape[0])
        img_xy_shifted_ = stitch_images(img_xy_shifted_list,image_size[1]-image_tiles[0].shape[1],image_size[0]-image_tiles[0].shape[0],image_tiles[0].shape[1],image_tiles[0].shape[0],image_size[1]//image_tiles[0].shape[1], image_size[0]//image_tiles[0].shape[0])
        return overlap_images(img_, img_x_shifted_, img_y_shifted_, img_xy_shifted_, image_tiles[0].shape[1], image_tiles[0].shape[0]) 

 
    # this code runs if LR!=None and overlap==False (Advanced stitching without overlapping tiles)
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


def get_shifted_images(images, total_width, total_height, width, height): #Returns all images in non shifted and 3 shifted versions
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
    return norm, x, y, xy

def get_simple_stitch(images, total_width, total_height, width, height): #Puts just tiles together in an image
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
    img = stitch_images(norm, total_width, total_height, width, height, k, n)
    return img

def main():             # TESTSCENARIO, will not be computated
    lr = cv2.cvtColor(cv2.imread(os.path.join("ISTest","img_lr.jpg")), cv2.COLOR_BGR2RGB)
    model = load_model(os.path.join("models","initialized_gen_model20.h5"), custom_objects={"tf":tf})
    lr_tiles_overlap = Utils.tile_image(lr, shape=(126,126), overlap=True)
    sr_tiles = []
    for tile in tqdm(lr_tiles_overlap):
        sr_tiles.append(   Utils.denormalize(np.squeeze(model.predict(np.expand_dims(Utils.rescale_imgs_to_neg1_1(tile), axis=0)), axis=0))  )
    simpleStitch = get_simple_stitch(sr_tiles, 4032, 3024, 504, 504)
    cv2.imwrite(os.path.join("ISTest","image_simple_stitched.jpg"), cv2.cvtColor(simpleStitch, cv2.COLOR_RGB2BGR))
    final_image = stitching(sr_tiles, lr, border_size=20, image_size=(3024,4032), LROffset = (0,0), overlap = True, adjustRGB=True)
    cv2.imwrite(os.path.join("ISTest","image_final.jpg"), cv2.cvtColor(final_image, cv2.COLOR_RGB2BGR))

if __name__ == "__main__":
    main()