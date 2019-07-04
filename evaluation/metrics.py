import os
import numpy as np 
import cv2
import math
from skimage.measure import compare_ssim as ssim


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
    calculates peak structural similarity index between prediction and groundtruth
    '''

    '''
    c1 = (k1 * L) ** 2
    c2 = (k2 * L) ** 2
    m_true = np.mean(true)
    m_pred = np.mean(pred)
    v_true = np.var(true)
    v_pred = np.var(pred)
    cov = np.cov( true.flatten(), pred.flatten() )[0, 1]

    return ((2 * m_pred * m_true + c1) * (cov + c2)) / ((m_pred**2 + m_true**2 + c1) * (v_pred + v_true + c2))
    '''

    return ssim(true, pred, multichannel=True)
