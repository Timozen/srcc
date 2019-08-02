import os
import numpy as np 
import cv2
import math
from skimage.measure import compare_ssim as ssim
from keras.models import load_model
import tensorflow as tf
from keras.models import Model
from keras.layers import Input
from data_generator import rescale_imgs_to_neg1_1
import Utils
from Utils_model import VGG_LOSS, MSE
import keras


def propagate(path, img):
    keras.backend.clear_session()
    #loss = VGG_LOSS((504,504,3))
    model = load_model(path, custom_objects={"tf":tf, "loss":MSE})

    _in = Input(shape=img.shape)
    _out = model(_in)
    _model = Model(_in, _out)

    if "Dense" in path:
        tmp = np.squeeze(_model.predict(np.expand_dims(img, axis=0)))
        #print(tmp.dtype)
        tmp[tmp < 0] = 0
        tmp[tmp > 255] = 255
        return tmp.astype(np.uint8)
    else:
        return Utils.denormalize(np.squeeze(_model.predict(np.expand_dims(rescale_imgs_to_neg1_1(img), axis=0)), axis=0))



def main():
    srdense = os.path.join("..", "models", "SRDense-Type-3_ep80.h5")
    srgan = os.path.join("..", "models", "srgan60.h5")
    srresnet = os.path.join("..", "models", "srresnet85.h5")

    #noise = (np.random.sample((300,300,3)) * 255).astype(np.uint8)
    test = cv2.cvtColor(cv2.imread("test_image.jpg"), cv2.COLOR_BGR2RGB)

    #cv2.imwrite("small-test.jpg", cv2.cvtColor(test,cv2.COLOR_RGB2BGR))
    #cv2.imwrite("hotel.jpg", cv2.cvtColor(cv2.resize(test, (0,0), fx=4, fy=4, interpolation=cv2.INTER_NEAREST), cv2.COLOR_RGB2BGR))

    g = cv2.cvtColor(propagate(srgan, test), cv2.COLOR_RGB2BGR)
    print(g)
    cv2.imwrite("srdense.jpg", cv2.cvtColor(propagate(srdense, test), cv2.COLOR_RGB2BGR))
    cv2.imwrite("srgan.jpg", g)
    cv2.imwrite("srresnet.jpg", cv2.cvtColor(propagate(srresnet, test), cv2.COLOR_RGB2BGR))


if __name__ == "__main__":
    main()