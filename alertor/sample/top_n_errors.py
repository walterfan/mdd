import pandas as pd
from tabulate import tabulate
import matplotlib.pyplot as plt


def top_n_errors(df, topCount=10):
    dfGroupByError = df.groupby(['errorReason'])['errorCount'].sum().reset_index()
    dfOfTopN = dfGroupByError.nlargest(topCount, 'errorCount', keep='first').reset_index(drop=True)
    print(tabulate(dfOfTopN, headers='keys', tablefmt="plain"))

    fig, ax = plt.subplots(figsize=(14, 8))
    plt.style.use('seaborn-whitegrid')
    plt.title('Top %d Errors Distribution' % topCount)
    plt.pie(dfOfTopN['errorCount'], labels=dfOfTopN['errorReason'], autopct='%1.1f%%', counterclock=False, shadow=True)

    plt.show()
    fig.savefig('top_n_errors.png')


if __name__ == '__main__':
    df = pd.read_csv('top_n_errors.csv')
    print(tabulate(df, headers='keys', tablefmt="grid"))
    top_n_errors(df)
