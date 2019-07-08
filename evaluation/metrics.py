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


def MSE(true, pred):
    '''
    calculates mean squared error between prediction and groundtruth
    '''
    return np.mean(np.square(true - pred))

def PSNR(true, pred, L=255):
    '''
    calculates peak signal noise ratio between prediction and groundtruth
    '''
    return 10 * math.log10( (L**2) / MSE(true, pred) )

def SSIM(true, pred, L=255, k1=0.01, k2=0.03):
    '''
    calculates the structural similarity index between prediction and groundtruth
    '''
    true = true.astype(np.float64)
    pred = pred.astype(np.float64)

    
    c1 = (k1 * L) ** 2
    c2 = (k2 * L) ** 2
    m_true = np.mean(true)
    m_pred = np.mean(pred)
    v_true = np.sqrt((1/(true.size - 1)) * np.sum( np.multiply((true-m_true),(true-m_true)) ))
    v_pred = np.sqrt((1/(true.size - 1)) * np.sum( np.multiply((pred-m_pred),(pred-m_pred)) ))
    cov = (1/(true.size - 1)) * np.sum( np.multiply((true-m_true),(pred-m_pred)) )

    return ((2 * m_pred * m_true + c1) * (2*cov + c2)) / ((m_pred**2 + m_true**2 + c1) * (v_pred**2 + v_true**2 + c2))
    #return ssim(true, pred, multichannel=True)


def MSSIM(true, pred, L=255, k1=0.01, k2=0.03, sub=8):
    """
    calculates the mean structural similarity index between prediction and groundtruth over sub*sub sub windows
    """
    stepsize_x = true.shape[1]//sub
    stepsize_y = true.shape[0]//sub

    mssim = 0.0
    for i in range(sub):
        for j in range(sub):
            mssim += SSIM(true[i*stepsize_y:(i+1)*stepsize_y, j*stepsize_x:(j+1)*stepsize_x, :], pred[i*stepsize_y:(i+1)*stepsize_y, j*stepsize_x:(j+1)*stepsize_x, :], L=L, k1=k1, k2=k2)

    return mssim/(sub**2)

def main():
    #true = cv2.imread(os.path.join("..", "DSIDS", "HR", "niklas_animal_0010.jpg"))
    true = cv2.cvtColor(cv2.imread(os.path.join("..", "DSIDS", "test", "20190702_210258.jpg")), cv2.COLOR_BGR2RGB)

    lr = cv2.resize(true, (0, 0),
                    fx=1/4,
                    fy=1/4,
                    interpolation=cv2.INTER_CUBIC)

    pred_nearest = cv2.resize(lr, (0, 0),
                      fx=4,
                      fy=4,
                      interpolation=cv2.INTER_CUBIC)


    model = load_model(os.path.join("..", "models", "initialized_gen_model20.h5"), custom_objects={"tf":tf})

    _in = Input(shape=lr.shape)
    _out = model(_in)
    _model = Model(_in, _out)

    sr = Utils.denormalize(np.squeeze(_model.predict(np.expand_dims(rescale_imgs_to_neg1_1(lr), axis=0)), axis=0))
    
    print("Nearest Neighbor:")
    print("MSE: ", MSE(true,pred_nearest))
    print("PSNR: ", PSNR(true,pred_nearest))
    print("SSIM: ", SSIM(true,pred_nearest))
    print("MSSIM: ", MSSIM(true,pred_nearest))

    print("Init SRGAN:")
    print("MSE: ", MSE(true,sr))
    print("PSNR: ", PSNR(true,sr))
    print("SSIM: ", SSIM(true,sr))
    print("MSSIM: ", MSSIM(true,sr))

    print("Black:")
    print("MSE: ", MSE(true,np.zeros(true.shape)))
    print("PSNR: ", PSNR(true,np.zeros(true.shape)))
    print("SSIM: ", SSIM(true,np.zeros(true.shape)))
    print("MSSIM: ", MSSIM(true,np.zeros(true.shape)))

    print("HR, HR:")
    print("MSE: ", MSE(true,true))
    print("PSNR: ", PSNR(true,true))
    print("SSIM: ", SSIM(true,true))
    print("MSSIM: ", MSSIM(true,true))

    cv2.imwrite("nearest.jpg", cv2.cvtColor(pred_nearest, cv2.COLOR_RGB2BGR))
    cv2.imwrite("sr.jpg", cv2.cvtColor(sr, cv2.COLOR_RGB2BGR))

if __name__ == "__main__":
    main()