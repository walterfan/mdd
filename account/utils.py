import os
import sys
import logging
import random
import base64
import string

def init_logger(filename):
    logger = logging.getLogger(filename)
    logger.setLevel(logging.INFO)

    formatstr = '%(asctime)s - [%(filename)s:%(lineno)d] - %(levelname)s - %(message)s'
    consoleHandler = logging.StreamHandler(sys.stdout)
    consoleHandler.setFormatter(logging.Formatter(formatstr))
    logger.addHandler(consoleHandler)

    logfile = os.path.join(".", filename + '.log')
    fileHandler = logging.FileHandler(logfile)
    logger.addHandler(fileHandler)
    return logger

def randomString(nLen=8):
    """Generate a random string of fixed length """
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(nLen))


def get_common_headers():
    http_headers = {}
    http_headers["Content-Type"] = "application/json"
    http_headers["TrackingID"] = randomString(32)

    return http_headers


def getAuthHeaders(clientId, clientSecret, tokenPrefix='Basic'):

    data = clientId + ':' + clientSecret

    encoded_data = base64.b64encode(data.encode())
    encoded_str = tokenPrefix + " " + encoded_data.decode()

    get_headers = {}
    get_headers["Authorization"] = tokenPrefix + encoded_str
    return get_headers


def build_account_request(siteName=None, siteUrl=None, userName='walter', password="pass"):
    account_request = {}
    if not siteName:
        siteName = randomString()
    if not siteUrl:
        siteUrl = "http://{}.test.com".format(siteName)

    account_request["siteName"] = siteName
    account_request["siteUrl"] = siteUrl
    account_request["userName"] = userName
    account_request["password"] = password

    return account_request