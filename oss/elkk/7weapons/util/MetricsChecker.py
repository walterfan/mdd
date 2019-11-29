import SearchTool as tool
import argparse
from datetime import datetime, timedelta
import pytz
import FileLogger
import MetricsReporter


class MetricsChecker:
    def __init__(self, category, env, alertName, alertItem):
        self.category = category
        self.env = env
        self.name = alertName
        self.failureQuery = alertItem.get('query', '')
        self.tiggerCount = alertItem.get('count', 0)
        self.isEnabled = alertItem.get('enabled', False)
        self.snoozedTime = alertItem.get('snoozed', '2019-01-01T00:00:00')
        self.priority = alertItem.get('priority', 1)
        self.suggestion = alertItem.get('suggestion', '')
        self.totalQuery = alertItem.get('query_scope', '')
        self.successRatio = alertItem.get('success_ratio', 0.99)

        self.reporter = MetricsReporter.MetricsReporter('Metrics Alert for ' + self.name, category, env)
        self.columns = ['metrics.event.timestamp',
                        'metrics.environment.address',
                        'metrics.event.name',
                        'metrics.event.responseCode',
                        'metrics.event.totalDurationInMS',
                        'metrics.event.method',
                        'metrics.event.endpoint',
                        'metrics.event.trackingID']

    def isSnoozed(self):
        if (not self.snoozedTime):
            return False
        today = datetime.now(pytz.timezone('UTC'))
        if (self.snoozedTime.replace(tzinfo=pytz.UTC) > today):
            print("will snooze", self.snoozedTime)
            return True
        else:
            print("will not snooze", self.snoozedTime)
            return False

    def needTriggeredByCount(self, foundCount):
        return foundCount >= self.tiggerCount

    def needTriggeredByRatio(self, failureCount, totalCount):
        if (totalCount > 0):
            ratio = 1 - failureCount / totalCount
            print(ratio)
            return ratio < self.successRatio
        return False

    def queryTotalCount(self, interval, **kwargs):

        beginTime = 'now-%s' % interval

        totalCnt = self.reporter.get_metrics_total_count(beginTime=beginTime,
                                                         endTime='now',
                                                         queryString=self.totalQuery,
                                                         size=1)
        return totalCnt

    def queryErrorRecords(self, interval, **kwargs):

        beginTime = 'now-%s' % interval
        size = kwargs.get('size', 10)

        df, cnt = self.reporter.get_metrics_records(beginTime=beginTime,
                                                    endTime='now',
                                                    queryString=self.failureQuery,
                                                    columns=self.columns,
                                                    size=size)

        return df, cnt

    def makeFailureSummary(self, df, interval, tablefmt='html'):
        beginTime = 'now-%s' % interval
        self.reporter.metrics_failure_summary(df, beginTime=beginTime,
                                              endTime='now',
                                              name=self.name,
                                              columns=self.columns,
                                              size=df.index.size,
                                              tablefmt=tablefmt)


def triggerAlerts(category, env, interval, emailList):
    alertItems = tool.alertConfig.get_alert_config_items()

    for key, value in alertItems:
        print(key, value)

        metricsChecker = MetricsChecker(category, env, key, value)

        if not metricsChecker.isEnabled:
            print("{} is not enabled".format(key))
            continue

        if metricsChecker.isSnoozed():
            print("{} is not snoozed".format(key))
            continue

        df, totalErrCnt = metricsChecker.queryErrorRecords(interval, size=10)

        queryErrCnt = df.index.size

        alertFlag = metricsChecker.needTriggeredByCount(totalErrCnt)

        if (metricsChecker.totalQuery):
            totalCount = metricsChecker.queryTotalCount('1d')
            alertFlag = alertFlag or metricsChecker.needTriggeredByRatio(totalErrCnt, totalCount)

            print("queryErrCnt={}, totalErrCnt={}, totalCount={}, alertFlag={}".format(queryErrCnt, totalErrCnt,
                                                                                       totalCount, alertFlag))

        if (alertFlag):
            print("trigger alert for {}".format(key))

            timeStr = FileLogger.getCurrentTimeStr('%Y-%m-%dT%H:%M:%S')
            metricsChecker.reporter.title = 'Metrics_Alert_Report_at_%s' % timeStr
            metricsChecker.makeFailureSummary(df, interval)
            metricsChecker.reporter.mdLogger.
            print("\n* Query string: {}".format(metricsChecker.failureQuery))
            metricsChecker.reporter.mdLogger.
            print("* Queried Metrics Error Count={}, Total Metrics Error Count={}, Total Metrics Count={}".format(
                queryErrCnt, totalErrCnt, totalCount))
            metricsChecker.reporter.sendEmail(emailList)


if __name__ == '__main__':

    parser = argparse.ArgumentParser()

    parser.add_argument('-i', action='store', dest='interval', help='specify query interval')
    parser.add_argument('-m', action='store', dest='email', help='specify an email list, divided by comma')
    parser.add_argument('-e', action='store', dest='env', help='specify an elastic search env')
    parser.add_argument('-j', action='store', dest='jobUrl', help='specify the job url')
    parser.add_argument('-s', action='store', dest='service', help='specify service name')
    args = parser.parse_args()

    service = 'Potato'
    emailList = ['walterfan@qq.com']
    env = 'LAB'

    jobUrl = 'http://10.224.76.69:8080'
    interval = "1d"

    if (args.email):
        emailList = [x.strip() for x in args.email.split(',')]

    if (args.env):
        env = args.env

    if (args.jobUrl):
        jobUrl = args.jobUrl

    if (args.interval):
        interval = args.interval

    if (args.service):
        service = args.service

    print("Check metrics for %s in %s" % (emailList, env))
    triggerAlerts(service, env, interval, emailList)
