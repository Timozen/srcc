import os
import numpy as np 
import cv2
from keras.models import load_model
import metrics
import Utils
from Utils_model import VGG_LOSS
from data_generator import rescale_imgs_to_neg1_1
import tensorflow as tf
from tqdm import tqdm
from keras.models import Model
from keras.layers import Input
import ImageStitching
import keras

WHOLE_LR = True
STITCHED = True

SAVE_PATH = "qual-eval"

def ggt(a, b):
    while b != 0:
        c = a % b
        a, b = b, c
    return a

def main():
    # paths to the models
    model_paths = [os.path.join("..", "models", "SRDense-Type-3_ep80.h5"), 
                   os.path.join("..", "models", "srdense-norm.h5"),
                   os.path.join("..", "models", "srresnet85.h5"),
                   os.path.join("..", "models", "gen_model90.h5"),
                   os.path.join("..", "models", "srgan60.h5"),
                   os.path.join("..", "models", "srgan-mse-20.h5"),
                   "Nearest"]

    # corresponding names of the models
    model_names = ["SRDense",
                   "SRDense-norm",
                   "SRResNet",
                   "SRGAN-from-scratch",
                   "SRGAN-percept-loss",
                   "SRGAN-mse",
                   "NearestNeighbor"]

    custom_objects = [{},
                      {"tf": tf},
                      {"tf": tf},
                      {"tf": tf},
                      {"tf": tf},
                      {"tf": tf},
                      {}]
    
    if not os.path.isdir(SAVE_PATH):
        os.makedirs(SAVE_PATH)

    test_image = cv2.cvtColor(cv2.imread(os.path.join(os.getcwd(), "test_image.jpg")), cv2.COLOR_BGR2RGB)

    lr_size = ggt(test_image.shape[0], test_image.shape[1])

    if lr_size == test_image.shape[0]:
        lr_size = int(lr_size/10)

    hr_size = lr_size*4
    lr_tiles = Utils.tile_image(test_image, shape=(lr_size, lr_size))
    lr_tiles_overlap = Utils.tile_image(test_image, shape=(lr_size, lr_size), overlap=True)

    if WHOLE_LR:
        '''
        First upscaling whole lr image
        '''

        for i,mp in tqdm(enumerate(model_paths)):
            keras.backend.clear_session()
            # first step: load the model
            if i < 6:
                model = load_model(mp, custom_objects=custom_objects[i])

                # second step: changing the input layer
                _in = Input(shape=test_image.shape)
                _out = model(_in)
                _model = Model(_in, _out)

            # third step propagating the image
            if i < 2:
                if i == 1:
                    lr = test_image.astype(np.float64)
                    lr = lr/255
                else:
                    lr = test_image
                tmp = np.squeeze(_model.predict(np.expand_dims(lr, axis=0)))
                if i == 1:
                    tmp = tmp*255
                tmp[tmp < 0] = 0
                tmp[tmp > 255] = 255
                sr = tmp.astype(np.uint8)
            elif i < 6:
                sr = Utils.denormalize(np.squeeze(_model.predict(np.expand_dims(rescale_imgs_to_neg1_1(test_image), axis=0)), axis=0))
            else:
                sr = cv2.resize(test_image, (0, 0),
                                fx=4,
                                fy=4,
                                interpolation=cv2.INTER_NEAREST)

            # fourth step saving the image
            cv2.imwrite(os.path.join(SAVE_PATH, model_names[i]+"_whole-lr.jpg"), cv2.cvtColor(sr, cv2.COLOR_RGB2BGR))


    if STITCHED:
        '''
        second upscaling tiles and stitching them together
        '''

        for i,mp in tqdm(enumerate(model_paths)):
            keras.backend.clear_session()
            # first step: load the model
            if i < 6:
                model = load_model(mp, custom_objects=custom_objects[i])

                # second step: changing the input layer
                _in = Input(shape=(lr_size, lr_size, 3))
                _out = model(_in)
                _model = Model(_in, _out)

            sr_tiles = []
            # third step propagating the tiles
            for tile in lr_tiles:
                if i < 2:
                    if i == 1:
                        lr = tile.astype(np.float64)
                        lr = lr/255
                    else:
                        lr = tile
                    tmp = np.squeeze(_model.predict(np.expand_dims(lr, axis=0)))
                    if i == 1:
                        tmp = tmp*255
                    tmp[tmp < 0] = 0
                    tmp[tmp > 255] = 255
                    sr_tiles.append( tmp.astype(np.uint8) )
                elif i < 6:
                    sr_tiles.append(Utils.denormalize(np.squeeze(_model.predict(np.expand_dims(rescale_imgs_to_neg1_1(tile), axis=0)), axis=0)))
                else:
                    sr_tiles.append(cv2.resize(tile, (0, 0),
                                    fx=4,
                                    fy=4,
                                    interpolation=cv2.INTER_NEAREST))
            # fourth step stitch the tiles
            sr_simple = ImageStitching.stitch_images(sr_tiles, hr_size*(test_image.shape[1]//lr_size), hr_size*(test_image.shape[0]//lr_size), 
                                                     sr_tiles[0].shape[1], sr_tiles[0].shape[0], test_image.shape[1]//sr_tiles[0].shape[1], 
                                                     test_image.shape[0]//sr_tiles[0].shape[0] )

            # fourth step saving the image
            cv2.imwrite(os.path.join(SAVE_PATH, model_names[i]+"_simple-stitched.jpg"), cv2.cvtColor(sr_simple, cv2.COLOR_RGB2BGR))

            # the same again for overlapping stitching
            sr_tiles_overlap = []
            # propagating the tiles
            for tile in lr_tiles_overlap:
                if i < 2:
                    if i == 1:
                        lr = tile.astype(np.float64)
                        lr = lr/255
                    else:
                        lr = tile
                    tmp = np.squeeze(_model.predict(np.expand_dims(lr, axis=0)))
                    if i == 1:
                        tmp = tmp*255
                    tmp[tmp < 0] = 0
                    tmp[tmp > 255] = 255
                    sr_tiles_overlap.append( tmp.astype(np.uint8) )
                elif i < 6:
                    sr_tiles_overlap.append(Utils.denormalize(np.squeeze(_model.predict(np.expand_dims(rescale_imgs_to_neg1_1(tile), axis=0)), axis=0)))
                else:
                    sr_tiles_overlap.append(cv2.resize(tile, (0, 0),
                                    fx=4,
                                    fy=4,
                                    interpolation=cv2.INTER_NEAREST))

            # stitch the tiles
            sr_overlap = ImageStitching.stitching(sr_tiles_overlap, LR=None, 
                                                  image_size=(hr_size*(test_image.shape[0]//lr_size), hr_size*(test_image.shape[1]//lr_size)),
                                                  adjustRGB=False, overlap=True)
            # save the image
            cv2.imwrite(os.path.join(SAVE_PATH, model_names[i]+"_overlap-stitched.jpg"), cv2.cvtColor(sr_overlap, cv2.COLOR_RGB2BGR))              
  


if __name__ == "__main__":
    main()
