from Network import Generator, Discriminator
from data_generator import create_data_generator, rescale_imgs_to_neg1_1
import Utils_model, Utils
from Utils_model import VGG_LOSS
from memory_usage import get_model_memory_usage

from keras.models import Model, load_model
from keras.layers import Input
from tqdm import tqdm
import numpy as np
import argparse
import os
import cv2


def check_model(path):
    model = load_model(path, custom_objects={'vgg_loss': VGG_LOSS.vgg_loss})
    model.summary()
    return model
