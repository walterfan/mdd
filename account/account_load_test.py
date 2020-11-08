import os
import string
import time
import json
import utils
from queue  import Queue
from locust import HttpUser, between, task
from locust import SequentialTaskSet

PERF_THRESHOLD_MS = 500

BASE_DIR = os.path.dirname(os.path.realpath(__file__))
DATE_FORMAT = '%Y-%m-%dT%H:%M:%S%z'
TEST_USER = "walter"
TEST_PASSWORD = "pass"

logger = utils.init_logger("account-load-test")

def check_response_time(start, end, name, trackingId):
    duration = end - start
    if duration > PERF_THRESHOLD_MS:
        logger.info("%s response too slow: %d, %s", name, duration, trackingId)

#------------------------ test cases ------------------------

class AccountTestSuite(SequentialTaskSet):

    account_queue = Queue()
    auth_headers = utils.getAuthHeaders(TEST_USER, TEST_PASSWORD)

    def on_start(self):
        logger.info("on_start")

    def on_stop(self):
        logger.info("on_stop")

    def list_account(self):
        self.client.get("/api/v1/accounts")


    @task(1)
    def create_account(self):

        http_headers = utils.get_common_headers()
        http_headers.update(self.auth_headers)

        post_dict = utils.build_account_request()

        post_data = json.dumps(post_dict)
        logger.info("http_headers: %s", json.dumps(http_headers))
        logger.info("http body: %s", post_data)
        start = time.time()
        response = self.client.post("/api/v1/accounts", headers=http_headers, data=post_data)
        check_response_time(start, time.time(), "create_account", http_headers['TrackingID'])
        if (200 <= response.status_code < 300):
            siteName = post_dict['siteName']
            logger.info("siteName: %s" % siteName)
            self.account_queue.put(siteName)

        return response

    @task(2)
    def retrieve_account(self):
        http_headers = utils.get_common_headers()
        http_headers.update(self.auth_headers)

        if not self.account_queue.empty():
            siteName = self.account_queue.get(True, 1)
            logger.info("retrieve_account by siteName %s", siteName)
            start = time.time()
            response = self.client.get("/api/v1/accounts/" + siteName, headers=http_headers,
                                       name="/api/v1/accounts/siteName")
            check_response_time(start, time.time(), "retrieve_account", http_headers['TrackingID'])
            logger.info("retrieve_account's response: %d, %s", response.status_code, response.text)
            self.account_queue.put(siteName)
        else:
            logger.warn("not account to retrieve")

    @task(3)
    def update_account(self):
        http_headers = utils.get_common_headers()
        http_headers.update(self.auth_headers)

        if not self.account_queue.empty():
            siteName = self.account_queue.get(True, 1)
            post_dict = utils.build_account_request()

            put_data = json.dumps(post_dict)
            start = time.time()
            response = self.client.put("/api/v1/accounts/" + siteName, headers=http_headers, data=put_data,
                                       name="/api/v1/accounts/siteName")
            check_response_time(start, time.time(), "update_account", http_headers['TrackingID'])
            logger.info("response: %d, %s", response.status_code, response.text)
            self.account_queue.put(siteName)
        else:
            logger.warn("not account to update")

    @task(4)
    def delete_account(self):
        http_headers = utils.get_common_headers()
        http_headers.update(self.auth_headers)

        if not self.account_queue.empty():
            siteName = self.account_queue.get(True, 1)
            start = time.time()
            response = self.client.delete("/api/v1/accounts/" + siteName, headers=http_headers,
                                          name="/api/v1/accounts/siteName")
            check_response_time(start, time.time(), "delete_account", http_headers['TrackingID'])
            logger.info("response: %d, %s", response.status_code, response.text)
        else:
            logger.warn("not account to delete")

#------------------------ simulate user to do testing ------------------------
class LocustTestUser(HttpUser):

    tasks = [AccountTestSuite]

    wait_time = between(0, 10)