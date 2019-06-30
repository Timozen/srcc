import os

import werkzeug
from flask import Flask, send_from_directory
from flask_restful import Api, Resource, reqparse
from werkzeug.utils import secure_filename

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
        args = parser.parse_args()

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

        # just echo the send image
        # from directory with a certain filename
        return send_from_directory(app.config['UPLOAD_FOLDER'],
                                   filename,
                                   as_attachment=True,
                                   attachment_filename=filename)


# register the api call addresses
api.add_resource(Home, '/api/Home')
api.add_resource(SendImage, '/api/SendImage')

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0")
