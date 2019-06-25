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

import matplotlib.pyplot as plt


image_shape = (336, 336, 3)
input_dirs = [os.path.join('..', '..', 'DSIDS', 'HR', 'tiles_'+str(image_shape[0])),
                  os.path.join('..', '..', 'DSIDS', 'LR', 'tiles_'+str(image_shape[0]) ,'4x_cubic')]

test_image = []
for img in os.listdir(os.path.join(input_dirs[1], 'ignore')):
    if 'niklas_city_0009' in img:
        test_image.append(rescale_imgs_to_neg1_1(cv2.imread(os.path.join(input_dirs[1], 'ignore', img))))
            
            
model = load_model(os.path.join("model", "gen_model5.h5"))
model.summary()
print(model.get_weights())
test = model.predict(np.expand_dims(test_image[42], axis=0))

print("test_image")
print(test)
#cv2.imwrite("test.jpg", Utils.denormalize(np.squeeze(test, axis=0)))
