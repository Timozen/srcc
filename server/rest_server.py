import os

import werkzeug
from flask import Flask, send_from_directory
from flask_restful import Api, Resource, reqparse
from werkzeug.utils import secure_filename
from io import BytesIO
from PIL import Image
import numpy as np
import os
import cv2
import tensorflow as tf
from Utils_model import VGG_LOSS
import Utils
from keras.models import Model
from keras.layers import Input
from keras.models import load_model
import ImageStitching
import keras

# helpful links
# https://flask-restful.readthedocs.io/en/latest/quickstart.html#a-minimal-api
# https://flask-restful.readthedocs.io/en/0.3.5/reqparse.html
# http://flask.pocoo.org/docs/0.12/patterns/fileuploads/
# https://stackoverflow.com/questions/28982974/flask-restful-upload-image
# https://medium.com/@suhas_chatekar/return-well-formed-error-responses-from-your-rest-apis-956b5275948

# define the static parameters
UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = set(['jpg', 'jpeg'])

# register all the important settings
app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
api = Api(app)


def load_backend_model(models, backend, tiling, img_shape, tile_size, use_init):
    '''Funktion that loads the respective backend model

    models          -- dictionary with the model paths
    backend         -- integer with the used backend
    tiling          -- bool should tiling be used
    tile_size       -- integer if tiling is used
    use_init        -- for backend 2 should initialization be used

    returns         -- loaded keras model  
    '''
    if backend == 0:
        print("load SRDense")
        model = load_model(models[backend])
        model.layers.pop(0)

        if tiling:
            _in = Input(shape=(tile_size, tile_size, 3))
        else:
            _in = Input(shape=img_shape)

        _out = model(_in)
        _model = Model(_in, _out)

    elif backend == 1:
        print("load SRResNet")
        loss = VGG_LOSS((504,504,3))
        model = load_model(models[backend], custom_objects={"tf": tf, "loss": loss.loss})
        model.layers.pop(0)

        if tiling:
            _in = Input(shape=(tile_size, tile_size, 3))
        else:
            _in = Input(shape=img_shape)

        _out = model(_in)
        _model = Model(_in, _out)

    elif backend == 2:
        if use_init:
            print("load initialized SRGAN")
        else:
            print("load SRGAN")
        model = load_model(models[backend][int(use_init)], custom_objects={"tf": tf})
        model.layers.pop(0)

        if tiling:
            _in = Input(shape=(tile_size, tile_size, 3))
        else:
            _in = Input(shape=img_shape)

        _out = model(_in)
        _model = Model(_in, _out)

    return _model


def sr_image(file_path, backend, tiling, tile_size, overlap, stitch_type, adjust_brightness, use_hsv, use_init):
    '''Funktion that calculates a superresolution to a given image

    file_path           -- path to the image
    backend             -- integer with the used backend
    tiling              -- bool should tiling be used
    tile_size           -- integer if tiling is used
    overlap             -- bool should overlapping tiles be used
    stitch_type         -- integer if tiling is used how should the sr_tiles be stitched together
    adjust_brightness   -- bool should the brightness be equalized
    use_hsv             -- bool should hsv colors of the lr image be used
    use_init            -- int for backend 2 should initialization be used

    returns             -- path to the sr_image    
    '''
    # clear any previous model
    keras.backend.clear_session()

    models = {0: os.path.join("models", "SRDense-Type-3_ep80.h5"),
              1: os.path.join("models", "init_gen_model50.h5"),
              2: (os.path.join("models", "gen_model90.h5"), os.path.join("models", "initialized_gen_model20.h5"))}

    # first step: load the image
    #img = Utils.crop_into_lr_shape( cv2.cvtColor( cv2.imread(file_path, cv2.IMREAD_COLOR), cv2.COLOR_BGR2RGB ) )
    img = cv2.cvtColor( cv2.imread(file_path, cv2.IMREAD_COLOR), cv2.COLOR_BGR2RGB )

    # second step: load the model
    model = load_backend_model(models, backend, tiling, img.shape, tile_size, use_init)

    # third step sr the image
    # check for tiling
    if tiling:
        if overlap:
            tiles = Utils.tile_image(img, shape=(tile_size,tile_size), overlap=True)
        else:
            tiles = Utils.tile_image(img, shape=(tile_size,tile_size))

        x_dim = img.shape[1] // tile_size
        y_dim = img.shape[0] // tile_size

        sr_tiles = []
        for tile in tiles:
            if backend == 0:
                sr_tiles.append( np.squeeze(model.predict(np.expand_dims(tile, axis=0)), axis=0) )
            else:
                sr_tiles.append( Utils.denormalize(np.squeeze(model.predict(np.expand_dims(Utils.rescale_imgs_to_neg1_1(tile), axis=0)), axis=0)))

        if stitch_type == 0:
            if overlap:
                sr = ImageStitching.stitching(sr_tiles, LR=None, image_size=(y_dim*sr_tiles[0].shape[0], x_dim*sr_tiles[0].shape[1]), overlap = True, adjustRGB = False)
            else:
                sr = ImageStitching.stitch_images(sr_tiles, x_dim*sr_tiles[0].shape[1], y_dim*sr_tiles[0].shape[0],
                                                  sr_tiles[0].shape[1], sr_tiles[0].shape[0], x_dim, y_dim)
        elif stitch_type == 1:
            if adjust_brightness and use_hsv:
                sr = ImageStitching.stitching(sr_tiles, LR = img, image_size=(y_dim*sr_tiles[0].shape[0], x_dim*sr_tiles[0].shape[1]), overlap = bool(overlap), adjustRGB = True)
            elif adjust_brightness:
                sr = ImageStitching.stitching(sr_tiles, LR = None, image_size=(y_dim*sr_tiles[0].shape[0], x_dim*sr_tiles[0].shape[1]), overlap = bool(overlap), adjustRGB = True)
            else:
                if overlap:
                    sr = ImageStitching.stitching(sr_tiles, LR=None, image_size=(y_dim*sr_tiles[0].shape[0], x_dim*sr_tiles[0].shape[1]), overlap = True, adjustRGB = False)
                else:
                    sr = ImageStitching.stitch_images(sr_tiles, x_dim*sr_tiles[0].shape[1], y_dim*sr_tiles[0].shape[0],
                                                      sr_tiles[0].shape[1], sr_tiles[0].shape[0], x_dim, y_dim)
    else:
        if backend == 0:
            sr = np.squeeze(model.predict(np.expand_dims(img, axis=0)), axis=0)
        else:
            sr = Utils.denormalize(np.squeeze(model.predict(np.expand_dims(Utils.rescale_imgs_to_neg1_1(img), axis=0)), axis=0))

    # save the sr image
    file_name = os.path.split(file_path)[1].split(".")[0] + "-sr.jpg"
    cv2.imwrite(os.path.join(os.path.split(file_path)[0], file_name), cv2.cvtColor(sr, cv2.COLOR_RGB2BGR))

    # clear the model
    keras.backend.clear_session()
    # return the name of the saved sr image
    return file_name

def allowed_file(filename):
    """
    Check if this file format is allowed.

    filename -- the filename to be checked if correct
    """
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


class Home(Resource):
    """
    The REST-Api response for the main directory.

    This will just return the name of the api.
    """

    def post(self):
        """
        Return the Post-Response upon request.

        This will just return the name 'srcc-rest-api'
        """
        return {'name': 'srcc-rest-api'}


class SendImage(Resource):
    """
    This class will handle the request on sending an image to the server.
    """

    def post(self):
        """
        This function handles the post request for this api request.

        This function will accept an post ruqest of an image. It will check
        if really an image was send and if it is usable.
        Currently it will just be saved and echoed to the client.
        TODO change the filename of the sendfile to something unique...
        """
        # create an parser of easily accessing the received data
        parser = reqparse.RequestParser()

        # we expect an image to be send, if not send message to the client
        parser.add_argument('image', required=True, type=werkzeug.datastructures.FileStorage, location='files')
        parser.add_argument('backend', required=True, type=int)
        parser.add_argument('initialization', required=False, type=int)
        parser.add_argument('tiling', required=True, type=int)
        parser.add_argument('tile_size', required=False, type=int)
        parser.add_argument('stitch_type', required=False, type=int)
        parser.add_argument('overlap', required=False, type=int)
        parser.add_argument('adjust_brightness', required=False, type=int)
        parser.add_argument('use_hsv', required=False, type=int)

        parser.add_argument('debug', required=False, type=int)

        args = parser.parse_args()

        if args['debug'] is not None and args['debug'] == 1:
            print(args)
            return {"msg" : "nan"}  

        # check if backend is in the correct range
        if not 0 <= args["backend"] <= 2:
            return "{'errorcode' : 'WRONG_BACKEND', 'field':'backend', 'message':'the backend is not in the correct range'}"

        # check if initialization is given when backend 2 is used:
        if args["backend"] == 2 and args["initialization"] is None:
            return "{'errorcode' : 'NO_INIT', 'field':'initialization', 'message':'backend 2 but no initialization specified'}"

        # check if tile_size and stitch type is given when tiling is used
        if args["tiling"] and (args["tile_size"] is None or args["stitch_type"] is None or args["overlap"] is None):
            return "{'errorcode' : 'NO_TILING_PARAMETERS', 'field':'tile_size, stitch_type, overlap', 'message':'tile_size,stitchtype or overlap is missing'}"
        
        # if stitch_type is given check if it is in the correct range
        if args["stitch_type"] is not None:
            if not 0 <= args["stitch_type"] <= 1:
                return "{'errorcode' : 'WRONG_STITCH_TYPE', 'field':'stitch_type', 'message':'the stitch_type is not in the correct range'}"
            if args["stitch_type"] == 1 and (args["adjust_brightness"] is None or args["use_hsv"] is None):
                return "{'errorcode' : 'NO_ADVANCED_PARAMETERS', 'field':'use_hsv, adjust_brightness', 'message':'advanced stitching parameters are missing'}"


        image = args['image']
        # check if the name is existing, real edge case, might never happen to us
        if image.filename == '':
            return "{'errorcode' : 'EMPTY_IMAGE_NAME', 'field':'image', 'message':'The image field doenst have image attached'}"

        # the image was empty...
        if not image:
            return "{'errorcode' : 'BROKEN_IMAGE', 'field':'image', 'message':'The send image was broken'}"

        # we do not accept the image format, or what ever format it is
        if not allowed_file(image.filename):
            return "{'errorcode' : 'NOT_ALLOWED_IMAGE_FORMAT', 'field':'image', 'message':'Image format now allowed'}"

        # TODO add some logging maybe...
        # get the filename for saving it in the upload folder
        filename = secure_filename(image.filename)
        image.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))

        # TODO in this part the NN has to create the SR image of the LR image
        sr_name = sr_image(os.path.join(UPLOAD_FOLDER, filename), args["backend"], 
                           args["tiling"], args["tile_size"], args["overlap"], args["stitch_type"], args["adjust_brightness"], args["use_hsv"], args["initialization"])
        

        # just echo the send image
        # from directory with a certain filename
        return send_from_directory(app.config['UPLOAD_FOLDER'],
                                   sr_name,
                                   as_attachment=True,
                                   attachment_filename=filename)


# register the api call addresses
api.add_resource(Home, '/api/Home')
api.add_resource(SendImage, '/api/SendImage')

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0")
