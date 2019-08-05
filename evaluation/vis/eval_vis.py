import chart_studio.plotly as py
import plotly.graph_objs as go

# libraries
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from pandas.plotting import parallel_coordinates
import matplotlib.pyplot as plt

"""
This script will create the data graphs for eval results.
2 types of graphs

to get the resuls of:
tile_performance.txt
whole_lr_performance.txt
we have to switch the hard coded string... yeah i am lazy
"""

# wäscheleine graph
df = pd.read_csv('whole_lr_performance.txt', sep=" ", header=None)
df.columns = ["Network", "MSE", "PSNR", "SSIM", "MSSIM"]
df['colorVal'] = [1,2,3,4,5,6,7]
data = [
    go.Parcoords(
        name = "Tile Performance",
        ids = df["Network"],
        line = dict(color = df['colorVal'],
                   colorscale = "Rainbow",
                   showscale = True,
                   reversescale = False,
                   cmin = 1,
                   cmax = 7,
                   colorbar = dict(tickmode="array",
                                thickness=10,
                                tickvals=df["colorVal"],
                                ticktext=df["Network"])
                    ),
        dimensions = list([
            dict(range = [0,100],
                label = 'MSE', values = df['MSE']),
            dict(range = [0,40],
                # constraintrange = [0,100],
                label = 'PSNR', values = df['PSNR']),
            dict(range = [0.7,1],
                label = 'SSIM', values = df['SSIM']),
            dict(range = [0.7,1],
                label = 'MSSIM', values = df['MSSIM'])  
        ])
    )
]

layout = go.Layout(
    title='Whole LR Performance'
)

fig = go.Figure(data = data, layout=layout)
fig.show()


def add_content(ax, simple, overlap, title):
    ind = np.arange(7)    # the x locations for the groups
    width = 0.35         # the width of the bars
    p1 = ax.bar(ind, simple, width)
    p2 = ax.bar(ind + width, overlap, width)

    ax.set_title(title)
    ax.set_xticks(ind + width / 2)
    ax.set_xticklabels(('SRDense', 'SRDense-norm', 'SRResNet', 'SRGAN-from-scratch', 'SRGAN-percept.-loss', 'SRGAN-mse', 'NearestNeighbor'))

    for tick in ax.get_xticklabels():
        tick.set_rotation(90)

    ax.legend((p1[0], p2[0]), ('Simple', 'Overlap'))
    ax.autoscale_view()

fig = plt.figure(figsize=(10,10))

df = pd.read_csv('stitch_performance.txt', sep=" ", header=None)
df.columns = ["Network", "MSE", "PSNR", "SSIM", "MSSIM"]

ax1 = fig.add_subplot(2, 2, 1)
val = df['MSE'].to_list()
simple = val[1::2]
overlap = val[::2]
add_content(ax1, simple, overlap, 'Simple vs Overlap MSE')

ax2 = fig.add_subplot(2, 2, 2)
val = df['MSE'].to_list()
simple = val[1::2]
overlap = val[::2]
add_content(ax2, simple, overlap, 'Simple vs Overlap PSNR')

ax3 = fig.add_subplot(2, 2, 3)
val = df['MSE'].to_list()
simple = val[1::2]
overlap = val[::2]
add_content(ax3, simple, overlap, 'Simple vs Overlap SSIM')

ax4 = fig.add_subplot(2, 2, 4)
val = df['MSE'].to_list()
simple = val[1::2]
overlap = val[::2]
add_content(ax4, simple, overlap, 'Simple vs Overlap MSSIM')

plt.tight_layout()
fig.savefig("simple_overlap.pdf")

