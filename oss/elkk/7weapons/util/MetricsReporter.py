# !/usr/bin/env python

import os
import traceback
import ChartHelper
import FileLogger
import SearchUtil as util
import SearchConfig as cfg
import SearchTool as tool
import argparse
import EmailSender
import pprint
from datetime import datetime, timedelta
import pytz

import pandas as pd
from tabulate import tabulate

pd.set_option('display.unicode.ambiguous_as_wide', True)
pd.set_option('display.unicode.east_asian_width', True)

pp = pprint.PrettyPrinter(indent=4)

logger = util.create_logger(os.path.basename("MetricsReporter"), False)


class MetricsReporter:
    def __init__(self, title="MetricsReporter", category='Potato', env='LAB'):
        self.title = title
        self.images = []
        self.csvFiles = []
        self.category = category
        self.env = env
        self.emailTemplate = cfg.get_home_path() + "/template/email_template.j2"

        self.make_report_files(title)

    def make_report_files(self, title):
        baseFileName = "./logs/{}_{}".format(self.title, FileLogger.getCurrentTimeStr('%Y%m%d%H%M%S'))

        self.mdLogger = FileLogger.MarkdownLogger(baseFileName + '.md')
        self.csvLogger = FileLogger.CsvLogger(baseFileName + '.csv', None, False)
        self.csvFiles.append(self.csvLogger.getFilePath())

    def get_els_results(self, queryText, elsUrl):
        searchTool = tool.SearchTool()
        status, results = searchTool.query(queryText, elsUrl)
        if (status >= 300):
            logger.error("response error: %d" % status)
            return status, results, 0

        totalCount = results['hits'].get('total', 0)
        if totalCount == 0:
            logger.info("not found %s " % elsUrl)
            return status, results, 0,

        return status, results, totalCount

    def get_results_as_df(self, queryText, elsUrl):
        status, results, totalCount = self.get_els_results(queryText, elsUrl)

        if (status >= 300 or results == None or totalCount == 0):
            return pd.DataFrame(data=None), 0

        return util.metrics_results_to_df(results), totalCount

    def get_metrics_records(self, **kwargs):
        beginTime = kwargs.get('beginTime', 'now-1d')
        endTime = kwargs.get('endTime', 'now')
        query = kwargs.get('query')
        size = kwargs.get('size', 100)

        category = kwargs.get('category', self.category)
        env = kwargs.get('env', self.env)

        queryString = kwargs.get('queryString')
        queryText = kwargs.get('queryText')

        tpl = kwargs.get('tpl', cfg.get_home_path() + '/template/time_sort_query.j2')

        unique_field = kwargs.get('unique_field')
        columns = kwargs.get('columns')
        if (not queryString):
            queryString = tool.getQueryString(category=category, name=query, **kwargs)
        if (not queryText):
            queryText = tool.getQueryText(size=size, beginTime=beginTime, endTime=endTime, query=queryString, tpl=tpl)

        elsList = tool.envConfig.get_els_urls(category, env)
        dfList = []
        totalCnt = 0
        if (elsList):
            for elsUrl in elsList:
                df, cnt = self.get_results_as_df(queryText, elsUrl)
                if (df.size > 0):
                    totalCnt = totalCnt + cnt
                    dfList.append(df)
        if (len(dfList) == 0):
            return pd.DataFrame(), 0

        large_df = pd.concat(dfList, axis=0, sort=False, ignore_index=True)
        if (unique_field and large_df.index.size > 0):
            drop_duplication_df = large_df.drop_duplicates([unique_field])
            if (columns):
                return drop_duplication_df[columns], totalCnt
            else:
                return drop_duplication_df, totalCnt
        else:
            if (columns):
                return large_df[columns], totalCnt
            else:
                return large_df, totalCnt

    def metrics_failure_summary(self, df, **kwargs):
        tablefmt = kwargs.get('tablefmt', 'html')
        self.mdLogger.
        print("\n## {} metrics: last {} records from {} to {} , now is {} in {}\n\n"
              .format(kwargs.get('name'),
                      kwargs.get('size'),
                      kwargs.get('beginTime'),
                      kwargs.get('endTime'),
                      FileLogger.getCurrentTimeStr('%Y-%m-%d %H:%M:%S'),
                      self.env))

        if (kwargs.get('columns')):
            mdTable = tabulate(df, headers=kwargs.get('columns'), tablefmt=tablefmt)
        else:
            mdTable = tabulate(df, headers='keys', tablefmt=tablefmt)
        self.mdLogger.append(mdTable)
        return mdTable

    def get_metrics_total_count(self, **kwargs):
        beginTime = kwargs.get('beginTime', 'now-1d')
        endTime = kwargs.get('endTime', 'now')
        query = kwargs.get('query')
        size = kwargs.get('size', 1)

        category = kwargs.get('category', self.category)
        env = kwargs.get('env', self.env)

        queryString = kwargs.get('queryString')
        queryText = kwargs.get('queryText')

        tpl = kwargs.get('tpl', cfg.get_home_path() + '/template/simple_query.j2')

        if (not queryString):
            queryString = tool.getQueryString(category=category, name=query, **kwargs)
        if (not queryText):
            queryText = tool.getQueryText(size=size, beginTime=beginTime, endTime=endTime, query=queryString, tpl=tpl)

        elsList = tool.envConfig.get_els_urls(category, env)

        totalCount = 0
        if (elsList):
            for elsUrl in elsList:
                status, results, cnt = self.get_els_results(queryText, elsUrl)
                totalCount = totalCount + cnt

        return totalCount

    def get_markdown(self, **kwargs):
        return self.mdLogger.getFileContent()

    def get_csvfiles(self, **kwargs):
        return self.csvFiles

    def get_csvfile(self, **kwargs):
        return self.csvLogger.getFilePath()

    def get_markdownfile(self, **kwargs):
        return self.mdLogger.getFilePath()

    def get_api_success_ratio(self, query, **kwargs):
        els = kwargs.get('els')
        queryString = tool.getQueryString(category=self.category, name=query, **kwargs)
        queryText = tool.getQueryText(query=queryString, tpl='template/api_success_ratio.j2', **kwargs)
        print(queryString, els)
        status, results = tool.search(queryText, els, "telephony")
        dataList = []
        if (status == 200):
            # print(results)
            for bucket in results.get('aggregations', {}).get('2', {}).get('buckets', []):
                # print(bucket)
                theTime = bucket.get('key_as_string')[:16]
                childBuckets = bucket.get('3').get('buckets')

                failCount = childBuckets.get("metrics.values.isSuccess:false", {}).get('doc_count')
                successCount = childBuckets.get("metrics.values.isSuccess:true", {}).get('doc_count')
                totalCount = failCount + successCount
                dataList.append((theTime, failCount, successCount, totalCount))
        else:
            print("error, status=%d" % status)
        labels = ['time', 'failCount', 'successCount', 'totalCount']
        df = pd.DataFrame(dataList, columns=labels)
        return df

    def get_api_top_errors(self, query, **kwargs):
        els = kwargs.get('els')
        queryString = tool.getQueryString(category=self.category, name=query, **kwargs)
        queryText = tool.getQueryText(query=queryString, tpl='template/top_n_errors.j2', **kwargs)
        print(queryString, els)
        status, results = tool.search(queryText, els, "telephony")
        dataList = []
        if (status == 200):
            # print(results)
            for bucket in results.get('aggregations', {}).get('2', {}).get('buckets', []):
                # print(bucket)
                errorReason = bucket.get('key')
                errorCount = bucket.get('doc_count')
                dataList.append((errorReason, errorCount))
        else:
            print("error, status=%d" % status)
        labels = ['errorReason', 'errorCount']
        df = pd.DataFrame(dataList, columns=labels)
        return df

    def sendEmail(self, recipients):

        mailer = EmailSender.EmailSender(os.getenv('EMAIL_USER'), mailServer=os.getenv('EMAIL_SMTP_SERVER'),
                                         mailPort=587, needLogin=True, useTls=True)

        if len(recipients) > 0:
            mailer.sendWithImages(self.title, recipients, self.mdLogger.toHtml(), self.emailTemplate,
                                  self.images, self.csvFiles)


if __name__ == '__main__':
    reporter = MetricsReporter("Potato Service Metrics Report")

    duration = "-1d"
    beginTime, endTime = util.calculateBeginEndTime(duration)
    env = 'LAB'

    itemName = 'potato_api_error'
    columns = ['environment.address', 'metrics.event.name', 'metrics.event.responseCode', 'metrics.event.method',
               'metrics.event.endpoint', 'metrics.event.trackingID']
    df, cnt = reporter.get_metrics_records(beginTime=beginTime,
                                           endTime='now',
                                           query=itemName,
                                           columns=columns,
                                           size=100,
                                           env=env)
    if (cnt > 0):
        reporter.metrics_failure_summary(df, beginTime=beginTime,
                                         endTime='now',
                                         query=itemName,
                                         size=100,
                                         env=env)

    reporter.sendEmail(['yafan@cisco.com', 'jiafu@cisco.com'])
