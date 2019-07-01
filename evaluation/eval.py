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


def main():
    # paths to the models
    model_paths = [os.path.join("..", "models", "SRDense-Type-3_ep4.h5"), 
                   os.path.join("..", "models", "SRDense-Type-3_ep80.h5"),
                   os.path.join("..", "models", "gen_model5.h5"),
                   os.path.join("..", "models", "gen_model90.h5"),
                   os.path.join("..", "models", "init_gen_model50.h5"),
                   os.path.join("..", "models", "initialized_gen_model20.h5")]

    # corresponding names of the models
    model_names = ["SRDense Type 3 after epoch 4",
                   "SRDense Type 3 after epoch 80",
                   "SRGAN github confg after 5 epochs",
                   "SRGAN github confg after 90 epochs",
                   "SRResnet after 50 epochs",
                   "SRGAN paper config after 20 epochs"]
    
    # corresponding tile shapes
    tile_shapes = [((168, 168), (42, 42)),
                   ((168, 168), (42, 42)),
                   ((336, 336), (82, 82)),
                   ((336, 336), (82, 82)),
                   ((504, 504), (126, 126)),
                   ((504, 504), (126, 126))]

    # used to load the models with custom loss functions
    loss = VGG_LOSS((504,504,3))
    custom_objects = [{},
                      {},
                      {"tf": tf},
                      {"tf": tf},
                      {"tf": tf, "loss": loss.loss},
                      {"tf": tf}]
    
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
        hr = cv2.imread(os.path.join(root, img), cv2.IMREAD_COLOR)
        lr = cv2.resize(hr, (0, 0),
                        fx=1/DOWN_SCALING_FACTOR,
                        fy=1/DOWN_SCALING_FACTOR,
                        interpolation=INTERPOLATION)
        test_images.append((lr, hr))


    '''
    First calculating performance metrics on single image tiles
    '''

    tile_performance = {}
    for i,mp in tqdm(enumerate(model_paths)):
        # first step: load the model
        model = load_model(mp, custom_objects=custom_objects[i])

        mse = []
        psnr = []
        ssim = []
        # second step: iterate over the test images
        for test_pair in test_images:
            # third step: tile the test image
            lr_tiles = Utils.tile_image(test_pair[0], shape=tile_shapes[i][1])
            hr_tiles = Utils.tile_image(test_pair[1], shape=tile_shapes[i][0])

            m = []
            p = []
            s = []

            # fourth step: iterate over the tiles
            for lr, hr in zip(lr_tiles, hr_tiles):
                # fifth step: calculate the sr tile
                if i > 1:
                    sr = Utils.denormalize(np.squeeze(model.predict(np.expand_dims(rescale_imgs_to_neg1_1(lr), axis=0)), axis=0))
                else:
                    sr = np.squeeze(model.predict(np.expand_dims(lr, axis=0))).astype(np.uint8)

                # sixth step: append the calculated metric
                m.append( metrics.MSE(hr, sr) )
                p.append( metrics.PSNR(hr, sr) )
                s.append( metrics.SSIM(hr, sr) )

            # eigth step: append the mean metric for this image 
            mse.append( np.mean(m) )
            psnr.append( np.mean(p) )
            ssim.append( np.mean(s) )
            
            # control
            print(mse)
            print(psnr)
            print(ssim)

        # ninth step: append the mean metric for this model
        tile_performance[model_names[i]] = "MSE = " + str(np.mean(mse)) + ", PSNR = " + str(np.mean(psnr)) + ", SSIM = " + str(np.mean(ssim))

    # final output
    print("Performance on single tiles:")
    for key in tile_performance:
        print(key, ":   ", tile_performance[key])

                
  


if __name__ == "__main__":
    main()