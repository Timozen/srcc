import matplotlib.pyplot as plt
import numpy as np
import imageio
import os

"""
This script is only for visualizing the results which can be found in the "images"
folder. It is adding a zoom effect
"""

def add_content_to_axes(ax, image, name):
    ax.imshow(image)
    ax.set_xticklabels('')
    ax.set_yticklabels('')
    # inset axes....
    axins = ax.inset_axes([0.7, 0.5, 0.25, 0.25])
    axins.imshow(image)
    # sub region of the original image
    x1, x2, y1, y2 = 300, 400, 250, 150
    axins.set_xlim(x1, x2)
    axins.set_ylim(y1, y2)
    axins.set_xticklabels('')
    axins.set_yticklabels('')
    ax.indicate_inset_zoom(axins, edgecolor="red", facecolor="red")

    axins2 = ax.inset_axes([0.7, 0.15, 0.25, 0.25])
    axins2.imshow(image)
    # sub region of the original image
    x12, x22, y12, y22 = 350, 450, 520, 420
    axins2.set_xlim(x12, x22)
    axins2.set_ylim(y12, y22)
    axins2.set_xticklabels('')
    axins2.set_yticklabels('')
    ax.indicate_inset_zoom(axins2, edgecolor="green", facecolor="green")

    ax.set_title(fr"${name}$")

# split the 21 images in 3*3 and 3*4 so it fits the latex document
k = 5
fig, ax = plt.subplots(3, 3, figsize=[3*k, 3*k])

for i, file_name in enumerate(os.listdir("images")):
    name = file_name.split(".")[0]
    name = name.replace("_", "-")
    image = imageio.imread(os.path.join("images", file_name))

    (x, y) = np.unravel_index(i, (3, 3))
    add_content_to_axes(ax[x, y], image, name)

    if i == 8:
        break

plt.tight_layout()
fig.savefig("image_vis_1.pdf")

fig, ax = plt.subplots(4, 3, figsize=[3*k, 4*k])
for i, file_name in enumerate(os.listdir("images")):
    if i < 9:
        continue

    index = i - 9
    name = file_name.split(".")[0]
    name = name.replace("_", "-")
    image = imageio.imread(os.path.join("images", file_name))

    (x, y) = np.unravel_index(index, (4, 3))
    add_content_to_axes(ax[x, y], image, name)

    

plt.tight_layout()
fig.savefig("image_vis_2.pdf")