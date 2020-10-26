import os
import sys
import logging
import random
import string
import time
import base64
import json
from queue  import Queue
from locust import HttpUser, between, task
from locust import SequentialTaskSet

PERF_THRESHOLD_MS = 500

BASE_DIR = os.path.dirname(os.path.realpath(__file__))
DATE_FORMAT = '%Y-%m-%dT%H:%M:%S%z'
TEST_USER = "walter"
TEST_PASSWORD = "pass"

#------------------------ common functions ------------------------

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


logger = init_logger("account-load-test")


def check_response_time(start, end, name, trackingId):
    duration = end - start
    if duration > PERF_THRESHOLD_MS:
        logger.info("%s response too slow: %d, %s", name, duration, trackingId)

#------------------------ test cases ------------------------

class AccountTestSuite(SequentialTaskSet):

    def on_start(self):
        logger.info("on_start")
        self.auth_headers = getAuthHeaders(TEST_USER, TEST_PASSWORD)
        self.account_queue = Queue()

    def on_stop(self):
        logger.info("on_stop, clear queue")

    def list_account(self):
        self.client.get("/api/v1/accounts")
        self.account_queue.clear()

    @task(1)
    def create_account(self):

        http_headers = get_common_headers()
        http_headers.update(self.auth_headers)

        post_dict = build_account_request()

        post_data = json.dumps(post_dict)
        logger.info("http_headers: %s", json.dumps(http_headers))
        logger.info("http body: %s", post_data)
        start = time.time()
        response = self.client.post("/api/v1/accounts", headers=self.auth_headers, data=post_data)
        check_response_time(start, time.time(), "create_account", http_headers['TrackingID'])
        if (200 <= response.status_code < 300):
            siteName = post_dict['siteName']
            logger.info("siteName: %s" % siteName)
            self.account_queue.put(siteName)

        return response

    @task(2)
    def retrieve_account(self):
        http_headers = get_common_headers()
        http_headers.update(self.auth_headers)

        if not self.account_queue.empty():
            siteName = self.account_queue.get(True, 1)
            logger.info("retrieve_account by siteName %s", siteName)
            start = time.time()
            response = self.client.get("/api/v1/accounts/" + siteName, headers=self.http_headers,
                                       name="/api/v1/accounts/siteName")
            check_response_time(start, time.time(), "retrieve_account", http_headers['TrackingID'])
            logger.info("retrieve_account's response: %d, %s", response.status_code, response.text)
            self.account_queue.put(siteName)

    @task(3)
    def update_account(self):
        http_headers = get_common_headers()
        http_headers.update(self.auth_headers)

        if not self.account_queue.empty():
            siteName = self.account_queue.get(True, 1)
            post_dict = build_account_request()

            put_data = json.dumps(post_dict)
            start = time.time()
            response = self.client.put("/api/v1/accounts" + siteName, headers=self.http_headers, data=put_data,
                                       name="/api/v1/accounts/siteName")
            check_response_time(start, time.time(), "update_account", http_headers['TrackingID'])
            logger.info("response: %d, %s", response.status_code, response.text)
            self.account_queue.put(siteName)

    @task(4)
    def delete_account(self):
        http_headers = get_common_headers()
        http_headers.update(self.auth_headers)

        if not self.account_queue.empty():
            siteName = self.account_queue.get(True, 1)
            start = time.time()
            response = self.client.delete("/api/v1/accounts/" + siteName, headers=self.http_headers,
                                          name="/api/v1/accounts/siteName")
            check_response_time(start, time.time(), "delete_account", http_headers['TrackingID'])
            logger.info("response: %d, %s", response.status_code, response.text)

#------------------------ simulate user to do testing ------------------------
class LocustForScenarioGroup(HttpUser):

    tasks = [AccountTestSuite]

    wait_time = between(0, 10)