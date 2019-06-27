'''
source: https://github.com/deepak112/Keras-SRGAN/blob/master/train.py

also:
C. Ledig et al., “Photo-Realistic Single Image Super-Resolution Using a Generative Adversarial Network,” in 2017 IEEE Conference on Computer Vision and Pattern Recognition (CVPR), Honolulu, HI, 2017, pp. 105–114.
'''


# title           :train.py
# description     :to train the model
# author          :Deepak Birla
# date            :2018/10/30
# usage           :python train.py --options
# python_version  :3.5.4

from __future__ import division
from Network import Generator, Discriminator
from data_generator import create_data_generator, rescale_imgs_to_neg1_1
import Utils_model, Utils
from Utils_model import VGG_LOSS, DENSE_LOSS, MSE
from memory_usage import get_model_memory_usage
from model_check import check_model

from keras.models import Model
from keras.layers import Input
from keras.utils import multi_gpu_model
from tqdm import tqdm
import numpy as np
import argparse
import os
import cv2

np.random.seed(10)


def train(img_shape, epochs, batch_size, rescaling_factor, input_dirs, output_dir, model_save_dir, train_test_ratio, gpu=1):    
    
    lr_shape = (img_shape[0]//rescaling_factor,
             img_shape[1]//rescaling_factor, img_shape[2])

    img_train_gen, img_test_gen = create_data_generator(
                                        input_dirs[1], input_dirs[0], 
                                        target_size_lr=(lr_shape[0], lr_shape[1]),
                                        target_size_hr=(img_shape[0],img_shape[1]),
                                        preproc_lr=rescale_imgs_to_neg1_1, 
                                        preproc_hr=rescale_imgs_to_neg1_1, 
                                        validation_split=train_test_ratio, batch_size=batch_size)

    batch_count = int((len(os.listdir(os.path.join(input_dirs[1], 'ignore'))) / batch_size)  * (1-train_test_ratio))

    test_image = []
    for img in sorted(os.listdir(os.path.join(input_dirs[1], 'ignore'))):
        if 'niklas_city_0009' in img:
            test_image.append(rescale_imgs_to_neg1_1(cv2.imread(os.path.join(input_dirs[1], 'ignore', img))))

    print("test length: ",len(test_image))
    
    loss = VGG_LOSS(image_shape)

    generator = Generator(lr_shape, rescaling_factor).generator()

    print('memory usage generator: ', get_model_memory_usage(batch_size, generator))

    optimizer = Utils_model.get_optimizer()
    
    if gpu > 1:
        try:
            print("multi_gpu_model generator")
            par_generator = multi_gpu_model(generator, gpus=2)
        except:
            par_generator = generator
            print("single_gpu_model generator")
    else:
        par_generator = generator
        print("single_gpu_model generator")
    
    par_generator.compile(loss=loss.loss, optimizer=optimizer)

    par_generator.summary()

    for e in range(1, epochs+1):
        print('-'*15, 'Epoch %d' % e, '-'*15)
        for i in tqdm(range(batch_count)):
            
            batch = next(img_train_gen)
            image_batch_hr = batch[1]
            image_batch_lr = batch[0]
            
            if image_batch_hr.shape[0] == batch_size and image_batch_lr.shape[0] == batch_size:
                g_loss = par_generator.train_on_batch(
                    image_batch_lr, image_batch_hr)
            else:
                print("weird multi_gpu_model batch error dis: ")
                print("hr batch shape: ", image_batch_hr.shape)
                print("lr batch shape: ", image_batch_lr.shape)

        if e == 1 or e % 5 == 0:
            Utils.generate_test_image(output_dir, e, generator, test_image)
        if e % 5 == 0:
            generator.save(os.path.join(model_save_dir , 'init_gen_model%d.h5' % e))


if __name__ == "__main__":
    image_shape = (336, 336, 3)

    epochs = 50
    batch_size = 16
    train_test_ratio = 0
    rescaling_factor = 4

    input_dirs = [os.path.join('..', '..', 'DSIDS', 'HR', 'tiles_'+str(image_shape[0])),
                  os.path.join('..', '..', 'DSIDS', 'LR', 'tiles_'+str(image_shape[0]) ,'4x_cubic')]
    output_dir = os.path.join(os.getcwd(), 'output_init')
    model_save_dir = os.path.join(os.getcwd(), 'model')

    if(not os.path.isdir(output_dir)):
        os.makedirs(output_dir)
    if(not os.path.isdir(model_save_dir)):
        os.makedirs(model_save_dir)

    train(image_shape, epochs, batch_size, rescaling_factor, input_dirs,
          output_dir, model_save_dir, train_test_ratio)
 
