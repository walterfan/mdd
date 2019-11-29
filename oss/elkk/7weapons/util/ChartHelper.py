import numpy as np
import matplotlib

matplotlib.use('Agg')
import matplotlib.pyplot as plt
import pandas as pd
import os
import time
import FileLogger
import traceback

DEFAULT_IMAGE_WIDTH = 10
DEFAULT_IMAGE_HEIGHT = 15


def strip(text):
    try:
        return text.strip()
    except AttributeError:
        return text


def str2date(text):
    return pd.to_datetime(text.strip())


def str2float(text):
    return float(text.strip())


def str2int(text):
    return int(text.strip())


class ChartHelper:
    def __init__(self, filename, title=None, isShow=True, isBlock=False, needChangePath=False, **kwargs):

        self.filename = filename
        self.title = title
        self.isShow = isShow
        self.isBlock = isBlock
        self.defaultStyle = ['bs-', 'ro-', 'y^-']

        plt.style.use('seaborn-whitegrid')

        self.fig = plt.figure()

        if (needChangePath):
            self.pngFile = "./logs/{}/{}.png".format(FileLogger.getCurrentTimeStr(), os.path.basename(self.filename))
        else:
            self.pngFile = filename

    def showDiagram(self):
        if (self.isShow):
            plt.show(block=self.isBlock)
            time.sleep(1)
            plt.close()

    def savePng(self, fig=None):

        if (fig):
            print("saved %s" % self.pngFile)
        else:
            print("saved %s" % self.pngFile)

            if (self.title):
                plt.title(self.title)

            plt.legend()

        self.showDiagram()

        directory = os.path.dirname(self.pngFile)
        if directory != '' and (not os.path.exists(directory)):
            os.makedirs(directory)

        if (fig):
            fig.savefig(self.pngFile)
        else:
            self.fig.savefig(self.pngFile)

    def draw(self, X, y, label=''):
        plt.xticks(rotation=30)
        plt.plot(X, y, label=label, marker='o', linestyle='solid', linewidth=2, markersize=6)

    def drawBarChartPerDc(self, df):
        df1 = df.groupby(['Date', 'DataCenter'])['Count'].sum()
        # print(df1)
        plt.style.use('seaborn-whitegrid')
        df1.plot(kind="bar", rot=30, figsize=(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT))
        self.showDiagram()

    def drawTopErrorsChart(self, df, topCount):
        df1 = df.groupby(['FailReason'])['Count'].sum()
        df2 = df1.nlargest(topCount)
        plt.style.use('seaborn-whitegrid')
        df2.plot(kind="bar", figsize=(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT))
        self.showDiagram()

    # df = pd.read_csv(csvFile, skipinitialspace=True, encoding='utf-8', error_bad_lines=False)
    def drawBarChart(self, df, period):
        df['timestamp'] = pd.to_datetime(df['timestamp'])
        df1 = df.groupby([df['timestamp'].dt.to_period(period)]).count()
        df2 = df1['timestamp']
        df2.plot(kind="bar", rot=30, style=self.defaultStyle, figsize=(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT))
        self.showDiagram()

    # df = pd.read_csv(csvFile, skipinitialspace=True, encoding='utf-8', error_bad_lines=False)
    def drawLineChart(self, df):
        df['timestamp'] = pd.to_datetime(df['Date'])
        plt.style.use('seaborn-whitegrid')
        # tab = df.pivot_table('Count', index='timestamp', columns=groupBy)
        tab = df.sort_values('Count', ascending=False).head(10)
        plt.bar(tab['Pool'], tab['Count'])
        self.showDiagram()
        return self.fig

    def drawChartPerPool(self, df, groupBy):
        df['timestamp'] = pd.to_datetime(df['Date'])
        plt.style.use('seaborn-whitegrid')
        tab = df.pivot_table('Count', index='timestamp', columns=groupBy)
        tab.plot(kind="line", rot=30, style=self.defaultStyle)
        self.showDiagram()
        return self.fig

    def getFileName(self):
        return self.pngFile


if __name__ == '__main__':
    helper = ChartHelper("test1.png", 'test image', True, True, dpi=100)

    helper.savePng()
