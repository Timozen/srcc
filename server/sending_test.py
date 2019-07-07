"""
This script is just for testing purposes to check if the server behaves correctly
"""

from requests import put, get, post
from io import BytesIO
from PIL import Image
import matplotlib.pyplot as plt

'''
# should fail and say it now allowed to do this kind of request
res = put('http://localhost:5000/api/SendImage', data={'data': 'Remember the milk'}).json()
print(res)'''

# send an image to the server
files = {'image': open('niklas_animal_0026.jpg', 'rb'), 'backend': (None,1), "tiling":(None, 1), "tile_size":(None, 126), "stitch_type":(None, 1), "overlap":(None, 1), "adjust_brightness":(None,1), "use_hsv":(None,1),  "initialization":(None, 1)}
res = post('http://localhost:5000/api/SendImage', files=files)

#print(res.text)

# the image should be return and displayed
image = Image.open(BytesIO(res.content))
plt.imshow(image)
plt.show()
