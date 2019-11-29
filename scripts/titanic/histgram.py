import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.mlab as mlab

titanic = pd.read_csv('./train.csv')
titanic.dropna(subset=['Age'], inplace=True)

plt.style.use('ggplot')

plt.hist(titanic.Age,
         bins=20,
         color='steelblue',
         edgecolor='k',
         label='Age histogram')

plt.tick_params(top='off', right='off')
plt.legend()
plt.show()
