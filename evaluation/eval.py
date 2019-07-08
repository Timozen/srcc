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

TILES = True
WHOLE_LR = True
STITCHED = True


def main():
    # paths to the models
    model_paths = [os.path.join("..", "models", "SRDense-Type-3_ep4.h5"), 
                   os.path.join("..", "models", "SRDense-Type-3_ep80.h5"),
                   os.path.join("..", "models", "gen_model5.h5"),
                   os.path.join("..", "models", "gen_model90.h5"),
                   os.path.join("..", "models", "init_gen_model50.h5"),
                   os.path.join("..", "models", "initialized_gen_model20.h5"),
                   "Nearest"]

    # corresponding names of the models
    model_names = ["SRDense4",
                   "SRDense80",
                   "SRGAN5",
                   "SRGAN90",
                   "SRResnet50",
                   "initSRGAN20",
                   "Nearest"]
    
    # corresponding tile shapes
    tile_shapes = [((168, 168), (42, 42)),
                   ((168, 168), (42, 42)),
                   ((336, 336), (84, 84)),
                   ((336, 336), (84, 84)),
                   ((504, 504), (126, 126)),
                   ((504, 504), (126, 126)),
                   ((336, 336), (336, 336))]

    # used to load the models with custom loss functions
    loss = VGG_LOSS((504,504,3))
    custom_objects = [{},
                      {},
                      {"tf": tf},
                      {"tf": tf},
                      {"tf": tf, "loss": loss.loss},
                      {"tf": tf},
                      {}]
    
    # creating a list of test images
    # [(lr, hr)]
    DOWN_SCALING_FACTOR = 4
    INTERPOLATION = cv2.INTER_CUBIC

    test_images = []
    root = os.path.join("..", "DSIDS", "test")
    # iterating over all files in the test folder
    for img in os.listdir(root):
        # chekcing if the file is an image
        if not ".jpg" in img:
            continue
        hr = Utils.crop_into_lr_shape(cv2.cvtColor(cv2.imread(os.path.join(root, img), cv2.IMREAD_COLOR), cv2.COLOR_BGR2RGB), shape=(3024,4032))
        lr = cv2.resize(hr, (0, 0),
                        fx=1/DOWN_SCALING_FACTOR,
                        fy=1/DOWN_SCALING_FACTOR,
                        interpolation=INTERPOLATION)
        test_images.append((lr, hr))

    if TILES:
        '''
        First calculating performance metrics on single image tiles
        '''

        tile_performance = {}
        for i,mp in tqdm(enumerate(model_paths)):
            # first step: load the model
            if i < 6:
                model = load_model(mp, custom_objects=custom_objects[i])

            mse = []
            psnr = []
            ssim = []
            mssim = []
            # second step: iterate over the test images
            for test_pair in tqdm(test_images):
                # third step: tile the test image
                lr_tiles = Utils.tile_image(test_pair[0], shape=tile_shapes[i][1])
                hr_tiles = Utils.tile_image(test_pair[1], shape=tile_shapes[i][0])

                m = []
                p = []
                s = []
                ms = []

                # fourth step: iterate over the tiles
                for lr, hr in zip(lr_tiles, hr_tiles):
                    # fifth step: calculate the sr tile
                    if i < 2:
                        sr = np.squeeze(model.predict(np.expand_dims(lr, axis=0))).astype(np.uint8)
                    elif i < 6:
                        sr = Utils.denormalize(np.squeeze(model.predict(np.expand_dims(rescale_imgs_to_neg1_1(lr), axis=0)), axis=0))
                    else:
                        sr = cv2.resize(lr, (0, 0),
                                        fx=4,
                                        fy=4,
                                        interpolation=cv2.INTER_NEAREST)

                    # sixth step: append the calculated metric
                    m.append( metrics.MSE(hr, sr) )
                    p.append( metrics.PSNR(hr, sr) )
                    s.append( metrics.SSIM(hr, sr) )
                    ms.append( metrics.MSSIM(hr, sr) )

                # seventh step: append the mean metric for this image 
                mse.append( np.mean(m) )
                psnr.append( np.mean(p) )
                ssim.append( np.mean(s) )
                mssim.append( np.mean(ms) )

            # eight step: append the mean metric for this model
            tile_performance[model_names[i]] = (np.mean(mse), np.mean(psnr), np.mean(ssim), np.mean(mssim))

        # final output
        print("Performance on single tiles:")
        f = open("tile_performance.txt", "w")
        for key in tile_performance:
            print(key+ ":   MSE = " + str(tile_performance[key][0]) + ", PSNR = " + str(tile_performance[key][1]) + ", SSIM = " + str(tile_performance[key][2]), ", MSSIM = " + str(tile_performance[key][3]))
            f.write(key+ " " + str(tile_performance[key][0]) + " " + str(tile_performance[key][1]) + " " + str(tile_performance[key][2]) + " " + str(tile_performance[key][3]) + "\n")
        f.close()


    if WHOLE_LR:
        '''
        Second calculating performance metrics on a single upscaled image
        '''

        img_performance = {}
        for i,mp in tqdm(enumerate(model_paths)):
            # first step: load the model
            if i < 6:
                model = load_model(mp, custom_objects=custom_objects[i])

                # second step: changing the input layer
                _in = Input(shape=test_images[0][0].shape)
                _out = model(_in)
                _model = Model(_in, _out)

            mse = []
            psnr = []
            ssim = []
            mssim = []
            # third step: iterate over the test images
            for test_pair in test_images:
                # fourth step: calculate the sr image
                try:
                    if i < 2:
                        sr = np.squeeze(_model.predict(np.expand_dims(test_pair[0], axis=0))).astype(np.uint8)
                    elif i < 6:
                        sr = Utils.denormalize(np.squeeze(_model.predict(np.expand_dims(rescale_imgs_to_neg1_1(test_pair[0]), axis=0)), axis=0))
                    else:
                        sr = cv2.resize(test_pair[0], (0, 0),
                                        fx=4,
                                        fy=4,
                                        interpolation=cv2.INTER_NEAREST)

                    # fifth step: append the metric for this image 
                    mse.append( metrics.MSE(test_pair[1], sr) )
                    psnr.append( metrics.PSNR(test_pair[1], sr) )
                    ssim.append( metrics.SSIM(test_pair[1], sr) )
                    mssim.append( metrics.MSSIM(test_pair[1], sr) )
                except:
                    mse.append("err")
                    psnr.append("err")
                    ssim.append("err")
                    mssim.append("err")

            # sixth step: append the mean metric for this model
            try:
                img_performance[model_names[i]] = (np.mean(mse), np.mean(psnr), np.mean(ssim), np.mean(mssim))
            except:
                img_performance[model_names[i]] = ("err", "err", "err", "err")

        # final output
        print("Performance on whole lr:")
        f = open("whole_lr_performance.txt", "w")
        for key in img_performance:
            print(key+ ":   MSE = " + str(tile_performance[key][0]) + ", PSNR = " + str(tile_performance[key][1]) + ", SSIM = " + str(tile_performance[key][2]), ", MSSIM = " + str(tile_performance[key][3]))
            f.write(key+ " " + str(tile_performance[key][0]) + " " + str(tile_performance[key][1]) + " " + str(tile_performance[key][2]) + " " + str(tile_performance[key][3]) + "\n")
        f.close()

    
    if STITCHED:
        '''
        Second calculating performance metrics on a stitched image
        '''

        stitch_performance = {}
        for i,mp in tqdm(enumerate(model_paths)):
            # first step: load the model
            model = load_model(mp, custom_objects=custom_objects[i])

            mse = []
            psnr = []
            ssim = []
            mssim = []

            o_mse = []
            o_psnr = []
            o_ssim = []
            o_mssim = []
            # second step: iterate over the test images
            for test_pair in test_images:
                # third step: tile the test image
                lr_tiles = Utils.tile_image(test_pair[0], shape=tile_shapes[i][1])
                lr_tiles_overlap = Utils.tile_image(test_pair[0], shape=tile_shapes[i][1], overlap=True)

                sr_tiles = []
                sr_tiles_overlap = []
                # fourth step: iterate over the tiles
                for lr in lr_tiles:
                    # fifth step: calculate the sr tiles
                    if i < 2:
                        sr_tiles.append( np.squeeze(model.predict(np.expand_dims(lr, axis=0))).astype(np.uint8) )
                    elif i < 6:
                        sr_tiles.append( Utils.denormalize(np.squeeze(model.predict(np.expand_dims(rescale_imgs_to_neg1_1(lr), axis=0)), axis=0)) )
                    else:
                        sr_tiles.append(cv2.resize(lr, (0, 0),
                                        fx=4,
                                        fy=4,
                                        interpolation=cv2.INTER_NEAREST))

                for lr in lr_tiles_overlap:
                    # fifth step: calculate the sr tiles
                    if i < 2:
                        sr_tiles_overlap.append( np.squeeze(model.predict(np.expand_dims(lr, axis=0))).astype(np.uint8) )
                    elif i < 6:
                        sr_tiles_overlap.append( Utils.denormalize(np.squeeze(model.predict(np.expand_dims(rescale_imgs_to_neg1_1(lr), axis=0)), axis=0)) )
                    else:
                        sr_tiles_overlap.append( cv2.resize(lr, (0, 0),
                                        fx=4,
                                        fy=4,
                                        interpolation=cv2.INTER_NEAREST))                      

                # sixth step: stitch the image
                sr_simple = ImageStitching.stitch_images(sr_tiles, test_pair[1].shape[1], test_pair[1].shape[0], sr_tiles[0].shape[1], sr_tiles[0].shape[0], test_pair[1].shape[1]//sr_tiles[0].shape[1], test_pair[1].shape[0]//sr_tiles[0].shape[0] )
                sr_advanced = ImageStitching.stitching(sr_tiles_overlap, LR=test_pair[0], image_size=(test_pair[1].shape[0], test_pair[1].shape[1]), adjustRGB=True, overlap=True)
                
                # seventh step: append the mean metric for this image 
                mse.append( metrics.MSE(test_pair[1], sr_simple) )
                psnr.append( metrics.PSNR(test_pair[1], sr_simple) )
                ssim.append( metrics.SSIM(test_pair[1], sr_simple) )
                mssim.append( metrics.MSSIM(test_pair[1], sr_simple) )

                o_mse.append( metrics.MSE(test_pair[1], sr_advanced) )
                o_psnr.append( metrics.PSNR(test_pair[1], sr_advanced) )
                o_ssim.append( metrics.SSIM(test_pair[1], sr_advanced) )
                o_mssim.append( metrics.MSSIM(test_pair[1], sr_advanced) )



            # ninth step: append the mean metric for this model
            stitch_performance[model_names[i]] = [(np.mean(mse), np.mean(psnr), np.mean(ssim), np.mean(mssim)), 
                                                  (np.mean(o_mse), np.mean(o_psnr), np.mean(o_ssim), np.mean(o_mssim))]

        # final output
        print("Performance on stitched images:")
        f = open("stitch_performance.txt", "w")
        for key in stitch_performance:
            print("simple stitch:  "+key+ ":   MSE = " + str(tile_performance[key][0][0]) + ", PSNR = " + str(tile_performance[key][0][1]) + ", SSIM = " + str(tile_performance[key][0][2]), ", MSSIM = " + str(tile_performance[key][0][3]))
            print("advanced stitch:  "+key+ ":   MSE = " + str(tile_performance[key][1][0]) + ", PSNR = " + str(tile_performance[key][1][1]) + ", SSIM = " + str(tile_performance[key][1][2]), ", MSSIM = " + str(tile_performance[key][1][3]))
            f.write(key+ " " + str(tile_performance[key][0][0]) + " " + str(tile_performance[key][0][1]) + " " + str(tile_performance[key][0][2]) + " " + str(tile_performance[key][0][3]) + "\n")
            f.write(key+ " " + str(tile_performance[key][1][0]) + " " + str(tile_performance[key][1][1]) + " " + str(tile_performance[key][1][2]) + " " + str(tile_performance[key][1][3]) + "\n")
        f.close()

                
  


if __name__ == "__main__":
    main()