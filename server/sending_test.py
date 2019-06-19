"""
This script is just for testing purposes to check if the server behaves correctly
"""

from requests import put, get, post
from io import BytesIO
from PIL import Image
import matplotlib.pyplot as plt

# should fail and say it now allowed to do this kind of request
res = put('http://localhost:5000/api/SendImage', data={'data': 'Remember the milk'}).json()
print(res)

# send an image to the server
files = {'image': open('test.jpg', 'rb')}
res = post('http://localhost:5000/api/SendImage', files=files)

# the image should be return and displayed
image = Image.open(BytesIO(res.content))
plt.imshow(image)
plt.show()
