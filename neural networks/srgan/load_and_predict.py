from __future__ import division
from Network import Generator, Discriminator
from data_generator import create_data_generator, rescale_imgs_to_neg1_1
import Utils_model, Utils
from Utils_model import VGG_LOSS, DENSE_LOSS
from memory_usage import get_model_memory_usage
from model_check import check_model
from keras.models import load_model

from keras.models import Model
from keras.layers import Input
from keras.utils import multi_gpu_model
from tqdm import tqdm
import numpy as np
import argparse
import os
import cv2
import tensorflow as tf
import Utils_images

import matplotlib.pyplot as plt


image_shape = (336, 336, 3)
#input_dirs = [os.path.join('..', '..', 'DSIDS', 'HR', 'tiles_'+str(image_shape[0])),
#                 os.path.join('..', '..', 'DSIDS', 'LR', 'tiles_'+str(image_shape[0]) ,'4x_cubic')]

#test_images = []
#for img in sorted(os.listdir(os.path.join(input_dirs[1], 'ignore'))):
#    if 'niklas_city_0009' in img:
#        test_images.append(rescale_imgs_to_neg1_1(cv2.imread(os.path.join(input_dirs[1], 'ignore', img))))
            
test = rescale_imgs_to_neg1_1(cv2.cvtColor(cv2.imread(os.path.join('..', '..', 'DSIDS' ,'LR', '4x_cubic', 'niklas_city_0009.jpg'),cv2.IMREAD_COLOR), cv2.COLOR_BGR2RGB))
#test = rescale_imgs_to_neg1_1(cv2.imread("1008.jpg",cv2.IMREAD_COLOR))

tile_list = Utils_images.crop_lr_image(test, hr_shape=(336,336), overlap=True)

model = load_model(os.path.join("model", "gen_model90.h5"), custom_objects={"tf": tf})
model.summary()
model.layers.pop(0)

_in = Input(shape=(756, 1008, 3))
#_in = Input(shape=(189, 252, 3))
_out = model(_in)

_model = Model(_in, _out)

test_ = cv2.cvtColor(Utils.denormalize(np.squeeze(_model.predict(np.expand_dims(test, axis=0)), axis=0)), cv2.COLOR_RGB2BGR)
cv2.imwrite(os.path.join("test.jpg"), test_)


'''
for i,tile in enumerate(tile_list):

    tile_ = Utils.denormalize(np.squeeze(model.predict(np.expand_dims(tile, axis=0)), axis=0))

    cv2.imwrite(os.path.join("overlap", format(i, "04d")+".jpg"), tile_)'''
