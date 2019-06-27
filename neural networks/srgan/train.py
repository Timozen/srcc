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
from Utils_model import VGG_LOSS, DENSE_LOSS
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


# Combined network
def get_gan_network(discriminator, shape, generator, optimizer, loss, batch_size):
    '''this function creates the GAN and compiles it

    discriminator -- the discriminator model
    shape -- the shape of the lr noise
    generator -- the generator model
    optimizer -- the optimizer used to optimize the gan
    loss -- the perceptural loss to compare generated and original img
    '''
    discriminator.trainable = False
    gan_input = Input(shape=shape)
    x = generator(gan_input)
    gan_output = discriminator(x)
    gan = Model(inputs=gan_input, outputs=[x, gan_output])
    
    print('memory usage gan: ', get_model_memory_usage(batch_size, gan))
    
    try:
        print("multi_gpu_model gan")
        par_gan = multi_gpu_model(gan, gpus=2)
    except:
        par_gan = gan
        print("single_gpu_model gan")

    par_gan.compile(loss=[loss, "binary_crossentropy"],
                loss_weights=[1., 1e-3],
                optimizer=optimizer)

    return gan, par_gan



def train(img_shape, epochs, batch_size, rescaling_factor, input_dirs, output_dir, model_save_dir, train_test_ratio):    
    
    lr_shape = (img_shape[0]//rescaling_factor,
             img_shape[1]//rescaling_factor, img_shape[2])

    img_train_gen, img_test_gen = create_data_generator(
                                        input_dirs[1], input_dirs[0], 
                                        target_size_lr=(lr_shape[0], lr_shape[1]),
                                        target_size_hr=(img_shape[0],img_shape[1]),
                                        preproc_lr=rescale_imgs_to_neg1_1, 
                                        preproc_hr=rescale_imgs_to_neg1_1, 
                                        validation_split=train_test_ratio, batch_size=batch_size)
    loss = VGG_LOSS(image_shape)

    batch_count = int((len(os.listdir(os.path.join(input_dirs[1], 'ignore'))) / batch_size)  * (1-train_test_ratio))

    test_image = []
    for img in sorted(os.listdir(os.path.join(input_dirs[1], 'ignore'))):
        if 'niklas_city_0009' in img:
            test_image.append(rescale_imgs_to_neg1_1(cv2.imread(os.path.join(input_dirs[1], 'ignore', img))))

    print("test length: ",len(test_image))

    generator = Generator(lr_shape, rescaling_factor).generator()
    discriminator = Discriminator(img_shape).discriminator()

    print('memory usage generator: ', get_model_memory_usage(batch_size, generator))
    print('memory usage discriminator: ', get_model_memory_usage(batch_size, discriminator))

    optimizer = Utils_model.get_optimizer()
    
    try:
        print("multi_gpu_model generator")
        par_generator = multi_gpu_model(generator, gpus=2)
    except:
        par_generator = generator
        print("single_gpu_model generator")
        
    try:
        print("multi_gpu_model discriminator")
        par_discriminator = multi_gpu_model(discriminator, gpus=2)
    except:
        par_discriminator = discriminator
        print("single_gpu_model discriminator")
    
    par_generator.compile(loss=loss.loss, optimizer=optimizer)
    par_discriminator.compile(loss="binary_crossentropy", optimizer=optimizer)

    gan, par_gan = get_gan_network(par_discriminator, lr_shape, par_generator,
                          optimizer, loss.loss, batch_size)
    
    par_discriminator.summary()
    par_generator.summary()
    par_gan.summary()

    loss_file = open(model_save_dir + 'losses.txt', 'w+')
    loss_file.close()

    for e in range(1, epochs+1):
        print('-'*15, 'Epoch %d' % e, '-'*15)
        
        if e == 100:
            optimizer.lr = 1e-5
        
        for i in tqdm(range(batch_count)):
            
            batch = next(img_train_gen)
            image_batch_hr = batch[1]
            image_batch_lr = batch[0]
            generated_images_sr = generator.predict(image_batch_lr)

            real_data_Y = np.ones(batch_size) - \
                np.random.random_sample(batch_size)*0.2
            fake_data_Y = np.random.random_sample(batch_size)*0.2

            par_discriminator.trainable = True
            
            if image_batch_hr.shape[0] == batch_size and image_batch_lr.shape[0] == batch_size:
                d_loss_real = par_discriminator.train_on_batch(
                    image_batch_hr, real_data_Y)
                d_loss_fake = par_discriminator.train_on_batch(
                    generated_images_sr, fake_data_Y)
                discriminator_loss = 0.5 * np.add(d_loss_fake, d_loss_real)
            else:
                print("weird multi_gpu_model batch error dis: ")
                print("hr batch shape: ", image_batch_hr.shape)
                print("lr batch shape: ", image_batch_lr.shape)
                print("gan y shape: ", gan_Y.shape)

            batch = next(img_train_gen)
            image_batch_hr = batch[1]
            image_batch_lr = batch[0]

            gan_Y = np.ones(batch_size) - \
                np.random.random_sample(batch_size)*0.2
            discriminator.trainable = False
            
            if image_batch_hr.shape[0] == batch_size and image_batch_lr.shape[0] == batch_size:
                gan_loss = par_gan.train_on_batch(image_batch_lr, [image_batch_hr, gan_Y])
            else:
                print("weird multi_gpu_model batch error gan: ")
                print("hr batch shape: ", image_batch_hr.shape)
                print("lr batch shape: ", image_batch_lr.shape)
                print("gan y shape: ", gan_Y.shape)

        print("discriminator_loss : %f" % discriminator_loss)
        print("gan_loss :", gan_loss)
        gan_loss = str(gan_loss)

        loss_file = open(model_save_dir + '_losses.txt', 'a')
        loss_file.write('epoch%d : gan_loss = %s ; discriminator_loss = %f\n' % (
            e, gan_loss, discriminator_loss))
        loss_file.close()

        if e == 1 or e % 5 == 0:
            Utils.generate_test_image(output_dir, e, generator, test_image)
        if e % 5 == 0:
            generator.save(os.path.join(model_save_dir , 'gen_model%d.h5' % e))
            discriminator.save(os.path.join(model_save_dir , 'dis_model%d.h5' % e))


if __name__ == "__main__":
    image_shape = (336, 336, 3)

    epochs = 200
    batch_size = 16
    train_test_ratio = 0.1
    rescaling_factor = 4

    input_dirs = [os.path.join('..', '..', 'DSIDS', 'HR', 'tiles_'+str(image_shape[0])),
                  os.path.join('..', '..', 'DSIDS', 'LR', 'tiles_'+str(image_shape[0]) ,'4x_cubic')]
    output_dir = os.path.join(os.getcwd(), 'output')
    model_save_dir = os.path.join(os.getcwd(), 'model')

    if(not os.path.isdir(output_dir)):
        os.makedirs(output_dir)
    if(not os.path.isdir(model_save_dir)):
        os.makedirs(model_save_dir)

    train(image_shape, epochs, batch_size, rescaling_factor, input_dirs,
          output_dir, model_save_dir, train_test_ratio)
