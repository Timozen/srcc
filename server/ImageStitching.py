import os
import numpy as np 
import cv2
import fnmatch
import Utils

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

    
    for i in range(rows + cols):
        for k in range(i):
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




def stitching(image_tiles, LR = None, border_size=20, image_size=(3024,4032)): 
    corrected_image_tiles = calc_border_factors(image_tiles,image_size[0]//image_tiles[0].shape[0],image_size[1]//image_tiles[0].shape[1])
    output = stitch_images(corrected_image_tiles,image_size[1], image_size[0],image_tiles[0].shape[0],image_tiles[0].shape[1], image_size[1]//image_tiles[0].shape[1], image_size[0]//image_tiles[0].shape[0])
    
    if LR is None: 
        return output
    
    HR_ = cv2.resize(LR, (0,0) , fx = 4, fy = 4, interpolation = cv2.INTER_CUBIC)
    hsv = cv2.cvtColor(HR_,cv2.COLOR_BGR2HSV)
    outputHSV = cv2.cvtColor(output,cv2.COLOR_BGR2HSV)
    outputHSV[:,:,0:2] = hsv[0:outputHSV.shape[0],0:outputHSV.shape[1],0:2]
    output = cv2.cvtColor(outputHSV,cv2.COLOR_HSV2BGR)
    return output

    


def main():
    '''
    overlap_images = load_images("","images")
    images = []
    for i in range(9+8):
        for j in range(12+11):
            if i%2 == 0 and j%2 == 0:
                images.append(overlap_images[i*23+j])
    '''
    image = cv2.imread(os.path.join("drive-download-20190702T161457Z-001","img_sr.jpg"))
    imageLR = cv2.imread(os.path.join("drive-download-20190702T161457Z-001","img_lr.jpg"))
    images = Utils.tile_image(image)

    final_image = stitching(images,imageLR)
    cv2.imwrite("test.jpg",final_image)

if __name__ == "__main__":
    main()