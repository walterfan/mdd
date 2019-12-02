import os
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
import time
import requests
import json
import SearchConfig as cfg
import SearchUtil as util

logger = util.create_logger("search_tool", True, 10)

alertConfig = cfg.AlertConfig(cfg.get_home_path() + "/config/alert_config.yml")
queryConfig = cfg.QueryConfig(cfg.get_home_path() + "/config/query_config.yml")
envConfig = cfg.EnvConfig(cfg.get_home_path() + "/config/env_config.yml")


class SearchTool:
    def __init__(self, username=os.getenv('ELS_USERNAME'), password=os.getenv("ELS_PASSWORD")):
        self.username = username
        self.password = password

    def __str__(self):
        return "..."

    def query(self, queryString, elsUrl):

        logger.debug("query %s : %s begin..." % (elsUrl, queryString));
        response = requests.get(elsUrl,
                                data=queryString,
                                headers={'content-type': 'application/json'},
                                auth=(self.username, self.password), verify=False)

        logger.debug("query %d : %s begin..." % (response.status_code, response.text))

        if (200 <= response.status_code < 300):
            results = json.loads(response.text)
            return response.status_code, results
        else:
            return response.status_code, response.text


def getQueryString(**kwargs):
    category = kwargs["category"]
    name = kwargs['name']

    queryStatement = queryConfig.get_query_string(category, name)

    queryStringBuilder = util.QueryStringBuilder(queryStatement)
    queryString = queryStringBuilder.build(**kwargs)

    return queryString


def getQueryText(**kwargs):
    tplName = kwargs.get('tpl', cfg.get_home_path() + "/template/simple_query.j2")

    queryTextBuilder = util.QueryTextBuilder(tplName)
    queryTextBuilder.size(kwargs.get('size', 1))

    queryTextBuilder.beginTime(kwargs.get('beginTime'))
    queryTextBuilder.endTime(kwargs.get('endTime'))

    queryString = kwargs.get('query', '')

    if (len(queryString) == 0):
        print("query is not specified %s" % queryString)
        logger.error("query is not specified %s" % queryString)
        exit(-1)

    queryTextBuilder.queryString(queryString)
    queryText = queryTextBuilder.build(**kwargs)

    return queryText


if __name__ == '__main__':
    queryString = "metrics.application.component:potato-web AND metrics.event.success:false"
    print("queryString is %s" % queryString)

    now = int(time.time())
    lastDay = now - 24 * 60 * 60 * 1000

    queryText = getQueryText(size=19, beginTime='now-1d', endTime='now', query=queryString)
    print(queryText)

    elsUrls = envConfig.get_els_urls()
    elsUrl = elsUrls[0]

    print("query {}".format(elsUrl))

    tool = SearchTool()
    status, results = tool.query(queryText, elsUrl)
    print("%s response code: %d" % (elsUrl, status))
    print("%s response content: %s" % (elsUrl, results))
