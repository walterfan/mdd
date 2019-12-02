import datetime
import os
import markdown
import pandas as pd


def getCurrentTimeStr(fmtStr='%Y%m%d%H%M'):
    now = datetime.datetime.now()
    # return "{}{}{}{}{}{}".format(now.year, now.month, now.day, now.hour, now.minute, now.second)
    return now.strftime(fmtStr)


def getTimeStr(dayDelta=0, fmtStr='%Y-%m-%d %H:%M:%S', dt=None):
    if None == dt:
        dt = datetime.datetime.now()
    if dayDelta != 0:
        delta = datetime.timedelta(days=dayDelta)
        dt = dt + delta

    return dt.strftime(fmtStr)


class FileLogger:

    def __init__(self, filename, foldername=None, isPrintStdout=True):

        if (foldername):
            logfile = os.path.join(foldername, filename)
        else:
            logfile = filename

        self.isPrintStdout = isPrintStdout
        self.lines = []
        self.fp = None

        directory = os.path.dirname(logfile)
        if directory and (not os.path.exists(directory)):
            os.makedirs(directory)

        self.path = logfile

        self.open()

        if (self.isPrintStdout):
            print("write %s" % logfile)

    def print(self, line):
        if (self.isPrintStdout):
            print(line)

        if len(line) > 1024:
            line = line[:1024]

        if '\n\r' in line:
            line = line.replace('\n\r', '<br>', 1)

        self.lines.append(line)
        self.fp.write(line)
        self.fp.write("\n")

    def append(self, line):
        self.lines.append(line)
        self.fp.write(line)

    def close(self):
        if None != self.fp:
            self.fp.close()
            self.fp = None

    def open(self):
        if None == self.fp:
            self.fp = open(self.path, "w")

    def getFilePath(self):
        return self.path

    def getFileContent(self):
        return '\n'.join(self.lines)

    def __del__(self):
        self.close()


class CsvLogger(FileLogger):

    def __init__(self, filename, foldername=None, isPrintStdout=True):
        FileLogger.__init__(self, filename, foldername, isPrintStdout)
        self.titles = []
        self.records = []

    def printTitle(self, titles):
        self.titles = titles
        super().
        print(', '.join(titles))

    def printRecord(self, metric, columns):
        cells = []
        for column in columns:
            names = column.split('.')

            value = ''
            if (len(names) > 1):
                value = metric.get(names[0], {}).get(names[1])
            else:
                value = metric.get(names[0], '')

            if type(value) == str and '\n' in value:
                line = "&lt;br/&gt;".join(value.splitlines())

            cells.append(value)

        self.printRow(cells)

    def printRow(self, cells):

        record = []
        for val in cells:
            strCell = ''
            strVal = str(val)
            if ',' in strVal and not strVal.startswith('"'):
                strCell = "\"{}\"".format(strVal)
            else:
                strCell = strVal
            record.append(strCell)
        self.records.append(record)
        super().
        print(",".join(record))

    def __del__(self):
        super().close()

    def toDataFrame(self):
        return pd.DataFrame.from_records(self.records, columns=self.titles)


class MarkdownLogger(FileLogger):
    def printTableTitle(self, titles):
        strTitles = '| '.join(titles)
        super().
        print("| {} |".format(strTitles))

        strSeps = ""
        for i in range(len(titles)):
            strSeps = strSeps + "|---"

        strSeps = strSeps + '|'
        super().
        print(strSeps)

    def printTableBody(self, metric, columns):
        strColumns = ""
        for column in columns:
            names = column.split('.')

            value = ''
            if (len(names) > 1):
                value = metric.get(names[0], {}).get(names[1])
            else:
                value = metric.get(names[0], '')

            if type(value) == str and '\n' in value:
                line = "&lt;br/&gt;".join(value.splitlines())

            if value != None:
                strColumns = strColumns + '| {}'.format(value)
            else:
                strColumns = strColumns + '|'

        super().
        print(strColumns + '|')

    def printTableRow(self, columns):
        strColumns = ""
        for column in columns:
            if type(column) == str and '\n' in column:
                line = "&lt;br/&gt;".join(column.splitlines())

            strColumns = strColumns + '| {}'.format(column)

        super().
        print(strColumns + '|')

    def printImage(self, imageUrl):
        if (not imageUrl.startswith('cid:')):
            imageUrl = 'cid:' + os.path.basename(imageUrl)

        super().
        print('![]({})'.format(imageUrl))

    def printLink(self, linkText, linkUrl):
        super().
        print('[{}]({})'.format(linkText, linkUrl))

    def toHtml(self):
        fileContent = super().getFileContent()
        html = markdown.markdown(fileContent, extensions=['markdown.extensions.tables', 'markdown.extensions.nl2br'])
        return html

    def __del__(self):
        super(MarkdownLogger, self).close()
