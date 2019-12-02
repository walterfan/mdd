import pandas as pd
from tabulate import tabulate
import matplotlib.pyplot as plt


def success_ratio(df):
    df1 = df.groupby(['time'])['failCount', 'successCount'].sum().reset_index()
    df1['totalCount'] = df1['successCount'] + df1['failCount']
    df1['success_ratio'] = df1['successCount'] / df1['totalCount']

    fig, ax = plt.subplots(figsize=(12, 8))
    plt.style.use('seaborn-whitegrid')
    chartDf = pd.DataFrame({'failCount': df1['failCount'].values,
                            'successCount': df1['successCount'].values,
                            'totalCount': df1['totalCount'].values
                            }, index=df1['time'])

    print(tabulate(chartDf, headers='keys', tablefmt="grid"))
    chartDf.plot.bar(rot=30, color=['r', 'g', 'b'], ax=ax)
    plt.show()
    fig.savefig('success_ratio.png')


if __name__ == '__main__':
    df = pd.read_csv('call_success_ratio.csv')
    print(tabulate(df, headers='keys', tablefmt="grid"))
    success_ratio(df)
