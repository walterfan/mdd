import numpy as np
from numpy.random import randn
import matplotlib.pyplot as plt


def scatter_chart(filename):
    fig = plt.figure()
    ax1 = fig.add_subplot(2, 1, 1)
    ax2 = fig.add_subplot(2, 1, 2)
    ax1.hist(randn(100), bins=20, color='k', alpha=0.3)
    ax2.scatter(np.arange(30), np.arange(30) + 3 * randn(30))
    plt.show()
    fig.savefig(filename)


if __name__ == "__main__":
    scatter_chart('scatter_chart.png')
