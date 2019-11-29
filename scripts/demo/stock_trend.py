# Wrote by Walter for Stock Trend 
import matplotlib.pyplot as plt
import pandas as pd

pd.core.common.is_list_like = pd.api.types.is_list_like
import pandas_datareader.data as web
import matplotlib
import time
import matplotlib.pyplot as plt
import argparse


def drawStockTrend(inc, startDate, endDate, pngFile):
    fig = matplotlib.pyplot.gcf()
    fig.set_size_inches(18.5, 10.5)

    df = web.DataReader(name=inc, data_source='iex', start=startDate, end=endDate)
    print(df)
    plt.style.use('seaborn-whitegrid')
    plt.xticks(rotation=30)
    plt.plot(df.index, df['open'], label='open', marker='o', linestyle=':', linewidth=1, markersize=3, color='gray')
    plt.plot(df.index, df['high'], label='high', marker='o', linestyle=':', linewidth=1, markersize=3, color='green')
    plt.plot(df.index, df['low'], label='low', marker='o', linestyle=':', linewidth=1, markersize=3, color='blue')
    plt.plot(df.index, df['close'], label='close', marker='o', linestyle='-', linewidth=2, markersize=6, color='red')

    for x, y in zip(df.index, df['close']):
        plt.text(x, y + 0.3, '%.2f' % y, ha='center', va='bottom', color='red')

    plt.legend()
    plt.title("%s' stock trend" % company)
    plt.show(block=True)
    time.sleep(1)

    if (not pngFile):
        fig.savefig(pngFile)

    plt.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()

    parser.add_argument('-c', action='store', dest='company', help='specify company')
    parser.add_argument('-s', action='store', dest='start', help='specify start date')
    parser.add_argument('-e', action='store', dest='end', help='specify end date')
    parser.add_argument('-f', action='store', dest='file', help='specify the filename')

    args = parser.parse_args()

    company = 'CSCO'
    startDate = '2019-01-01'
    endDate = '2019-02-19'
    pngFile = None

    if (args.company):
        company = args.company

    if (args.start):
        startDate = args.start

    if (args.end):
        endDate = args.end

    if (args.file):
        pngFile = args.file

    drawStockTrend(company, startDate, endDate, pngFile)

    # usage example:
    # python stock_trend.py -c GOOGL -s 2019-01-01 -e 2019-02-19 -f google_stock_trend.png
    # python stock_trend.py -c CSCO -s 2019-01-01 -e 2019-02-19 -f cisco_stock_trend.png
    # python stock_trend.py -c SINA -s 2019-01-01 -e 2019-02-19 -f sina_stock_trend.png
    # python stock_trend.py -c BIDU -s 2019-01-01 -e 2019-02-19 -f baidu_stock_trend.png
    # python stock_trend.py -c NTES -s 2019-01-01 -e 2019-02-19 -f netease_stock_trend.png
