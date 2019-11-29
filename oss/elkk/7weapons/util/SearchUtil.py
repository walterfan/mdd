import logging
import sys
import os
import time
import collections
import pandas as pd
from pytz import timezone
from datetime import datetime, timedelta

from jinja2 import Template


def calculateBeginEndTime(interval, fromZero=True):
    today = datetime.now(timezone('UTC'))
    if (fromZero):
        today = today.replace(hour=0, minute=0, second=0)

    if ('d' in interval):
        duration = interval.replace('d', '')
        delta = timedelta(days=int(duration))
    elif ('h' in interval):
        duration = interval.replace('h', '')
        delta = timedelta(hours=int(duration))
    elif ('w' in interval):
        duration = interval.replace('w', '')
        delta = timedelta(weeks=int(duration))
    else:
        print("not support format %s" % interval)

    endTime = today + timedelta(seconds=-1)
    beginTime = today + delta
    return beginTime.strftime('%Y-%m-%dT%H:%M:%S'), endTime.strftime('%Y-%m-%dT%H:%M:%S')


def flatten(metricsSource, parent_key='', sep='.', ignoreKeys=['message', 'beat']):
    items = []
    for k, v in metricsSource.items():
        if (k in ignoreKeys):
            continue
        new_key = parent_key + sep + k if parent_key else k
        if isinstance(v, collections.MutableMapping):
            items.extend(flatten(v, new_key, sep=sep).items())
        else:
            items.append((new_key, v))
    return dict(items)


def metrics_log_to_df(metrics_log, ignoreKeys=['message', 'beat']):
    metrics_src = metrics_log.get('_source')
    metrics_dict = flatten(metrics_src, '', '.', ignoreKeys)
    df = pd.DataFrame.from_dict(metrics_dict, orient='index').T
    return df


def metrics_results_to_df(results_dict, ignoreKeys=['message', 'beat']):
    dfList = []
    hitsList = results_dict.get("hits").get("hits")
    for hit in hitsList:
        df = metrics_log_to_df(hit, ignoreKeys)
        dfList.append(df)
    return pd.concat(dfList, axis=0, sort=False, ignore_index=True)


class QueryStringBuilder:

    def __init__(self, str=""):
        self.parameters = {}
        self.queryString = str

    def poolName(self, poolName):
        self.parameters["poolName"] = poolName

    def featureName(self, featureName):
        self.parameters["featureName"] = featureName

    def featureNames(self, featureNames):
        for featureName in featureNames:
            self.queryString = self.queryString + (" OR metrics.featureName:%s " % featureName)

    def poolNames(self, poolNames):
        for poolName in poolNames:
            self.queryString = self.queryString + (" OR metrics.poolName:%s " % poolName)

    def build(self, **kwargs):
        params = dict(**kwargs)
        if (bool(params)):
            template = Template(self.queryString)
            return template.render(params)

        return self.queryString


class QueryTextBuilder:
    def __init__(self, tpl="template/simple_query.j2"):
        self.parameters = {}
        self.template_file = tpl

    def size(self, size):
        self.parameters['size'] = size

    def beginTime(self, beginTime):
        self.parameters['beginTime'] = beginTime

    def endTime(self, endTime):
        self.parameters['endTime'] = endTime

    def queryString(self, queryString):
        self.parameters['queryString'] = queryString

    def build(self, **kwargs):
        self.parameters.update(kwargs)
        json_content = ""
        with open(self.template_file, "r") as src_file:
            template = Template(src_file.read())
            json_content = template.render(self.parameters)
        # print(json_content)
        return json_content


def create_logger(filename, log2console=True, logLevel=logging.INFO, logFolder='./logs'):
    # add log
    logger = logging.getLogger(filename)
    logger.setLevel(logging.INFO)
    formatstr = '%(asctime)s - [%(filename)s:%(lineno)d] - %(levelname)s - %(message)s'
    formatter = logging.Formatter(formatstr)

    logfile = os.path.join(logFolder, filename + '.log')
    directory = os.path.dirname(logfile)
    if not os.path.exists(directory):
        os.makedirs(directory)

    handler = logging.FileHandler(logfile)
    handler.setLevel(logLevel)
    handler.setFormatter(formatter)
    logger.addHandler(handler)

    if log2console:
        handler2 = logging.StreamHandler(sys.stdout)
        handler2.setFormatter(logging.Formatter(formatstr))
        handler2.setLevel(logLevel)
        logger.addHandler(handler2)

    return logger
