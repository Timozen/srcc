'''
source: https://github.com/deepak112/Keras-SRGAN/blob/master/Utils_model.py

also:
C. Ledig et al., “Photo-Realistic Single Image Super-Resolution Using a Generative Adversarial Network,” in 2017 IEEE Conference on Computer Vision and Pattern Recognition (CVPR), Honolulu, HI, 2017, pp. 105–114.
'''


#title           :Utils_model.py
#description     :Have functions to get optimizer and loss
#author          :Deepak Birla
#date            :2018/10/30
#usage           :imported in other files
#python_version  :3.5.4

from keras.applications.vgg19 import VGG19
from keras.applications.densenet import DenseNet121
import keras.backend as K
from keras.models import Model
from keras.optimizers import Adam

def MSE(y_true, y_pred):
    return K.mean(K.square(y_true - y_pred))
    

class VGG_LOSS(object):

    def __init__(self, image_shape):
        
        self.image_shape = image_shape

    # computes VGG loss or content loss
    def loss(self, y_true, y_pred):
        vgg19 = VGG19(include_top=False, weights='imagenet', input_shape=self.image_shape)
        
        vgg19.trainable = False
        # Make trainable as False
        for l in vgg19.layers:
            l.trainable = False
        model = Model(inputs=vgg19.input, outputs=vgg19.get_layer('block5_conv4').output)
        model.trainable = False
    
        return 0.006* K.mean(K.square(model(y_true) - model(y_pred)))

class DENSE_LOSS(object):

    def __init__(self, image_shape):
        
        self.image_shape = image_shape

    # computes VGG loss or content loss
    def loss(self, y_true, y_pred):
        densenet = DenseNet121(include_top=False, weights='imagenet', input_shape=self.image_shape)
        
        densenet.trainable = False
        # Make trainable as False
        for l in densenet.layers:
            l.trainable = False

        model = Model(inputs=densenet.input, outputs=densenet.layers[-1].output)
        model.trainable = False
    
        return 0.006 * K.mean(K.square(model(y_true) - model(y_pred)))
    
    
def get_optimizer():
 
    adam = Adam(lr=1e-4, beta_1=0.9, beta_2=0.999, epsilon=1e-08)
    return adam
