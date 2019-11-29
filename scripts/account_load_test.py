from locust import HttpLocust, TaskSet, task, seq_task

import load_test_util
import json
import yaml

from queue  import Queue
from threading import Timer
logger = load_test_util.init_logger("account-load-test")
token_refresh_time = 300



class UserBehavior(TaskSet):

    def on_start(self):
        logger.info("on_start")
        self.auth_headers = load_test_util.getAuthHeaders()
        self.account_queue = Queue()

    def on_stop(self):
        logger.info("on_stop, clear queue")

    def list_account(self):
        self.client.get("/api/v1/accounts")

    @seq_task(1)
    def create_account(self):
        post_dict = load_test_util.create_account_request()

        post_data = json.dumps(post_dict)

        logger.info("auth_headers: %s", json.dumps(self.auth_headers))
        logger.info("post_data: %s", post_data)

        response = self.client.post("/api/v1/accounts", headers = self.auth_headers, data=post_data)
        logger.info("response: %d, %s", response.status_code, response.text)
        if (200 <= response.status_code < 300):
            siteName = post_dict['siteName']
            logger.info("siteName: %s" % siteName)
            self.account_queue.put(siteName)

        return response

    @seq_task(2)
    def retrieve_account(self):

        if not self.account_queue.empty():
            siteName = self.account_queue.get(True, 1)
            logger.info("retrieve_account by siteName %s", siteName)
            response = self.client.get("/api/v1/accounts/" + siteName, headers=self.auth_headers, name="/api/v1/accounts/siteName")
            logger.info("retrieve_account's response: %d, %s", response.status_code, response.text)
            self.account_queue.put(siteName)


    @seq_task(3)
    def update_account(self):
         if not self.account_queue.empty():
            siteName = self.account_queue.get(True, 1)
            post_dict = load_test_util.create_account_request()

            put_data = json.dumps(post_dict)
            response = self.client.put("/api/v1/accounts"+ siteName, headers = self.auth_headers, data=put_data, name="/api/v1/accounts/siteName")
            logger.info("response: %d, %s", response.status_code, response.text)
            self.account_queue.put(siteName)

    @seq_task(4)
    def delete_account(self):
        if not self.account_queue.empty():
            siteName = self.account_queue.get(True, 1)
            response = self.client.delete("/api/v1/accounts/" + siteName, headers = self.auth_headers, name="/api/v1/accounts/siteName")
            logger.info("response: %d, %s", response.status_code, response.text)

class WebsiteUser(HttpLocust):
    task_set = UserBehavior
    min_wait = 500
    max_wait = 3000