from pandas.plotting import table
import pandas as pd
from tabulate import tabulate
import matplotlib.pyplot as plt


def apdex(df):
    df['total_requests'] = df['fast'] + df['slow'] + df['fail']
    print(tabulate(df, headers='keys', tablefmt="grid"))

    fig, ax = plt.subplots(figsize=(16, 8))

    ax1 = plt.subplot(121, aspect='equal')
    df.plot(kind='pie', y='total_requests', ax=ax1, autopct='%1.1f%%',
            startangle=90, shadow=False, labels=df['data_center'], legend=False, fontsize=14)

    # plot table
    ax2 = plt.subplot(122)
    plt.axis('off')
    tbl = table(ax2, df[['data_center', 'total_requests', 'fast', 'slow', 'fail']], loc='center')
    tbl.scale(1.5, 1.5)
    tbl.auto_set_font_size(False)
    tbl.set_fontsize(14)
    plt.show()
    fig.savefig('apdex.png')


if __name__ == '__main__':
    df = pd.read_csv('apdex.csv')
    apdex(df)
