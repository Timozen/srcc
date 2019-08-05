import numpy as np 
import matplotlib.pyplot as plt
import pandas as pd


'''
Plotting dataset histogram
'''

fig, ax = plt.subplots(nrows=1, ncols=1)
ax.set_title('Categories Histogram')

raw_data = {'Tim': [61, 20, 15, 40, 26, 5, 21, 24, 21], 'Simon': [43, 23, 39, 22, 8, 10, 2, 2, 6],'Niklas': [75, 63, 31, 41, 30, 47, 25, 16, 30]}
df = pd.DataFrame(raw_data)

cat = ["city", "landscape", "food", "object", "people", "animal", "texture", "car", "plant"]
x = np.array(range(len(cat)))

barWidth = 0.85

# Create green Bars
plt.bar(x, df['Tim'], color='#9aace3', edgecolor='white', width=barWidth, label='Tim')
# Create orange Bars
plt.bar(x, df['Simon'], bottom=df['Tim'], color='#e39a9a', edgecolor='white', width=barWidth, label='Simon')
# Create blue Bars
plt.bar(x, df['Niklas'], bottom=[i+j for i,j in zip(df['Tim'], df['Simon'])], color='#9ae3ae', edgecolor='white', width=barWidth, label='Niklas')

plt.xticks(x, cat)
plt.xlabel("categories")
plt.setp(plt.xticks()[1], rotation=30)

for tick in ax.xaxis.get_major_ticks():
                tick.label.set_fontsize(12) 

params = {'legend.fontsize': 12,
          'legend.handlelength': 2}
plt.rcParams.update(params)
plt.legend()

plt.savefig('categories_hist.pdf')

plt.show()
