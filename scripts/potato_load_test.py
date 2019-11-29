from locust import HttpLocust, TaskSet, task, seq_task

import load_test_util
import json
import yaml

from queue  import Queue
from threading import Timer
logger = load_test_util.init_logger("potato-load-test")
token_refresh_time = 300



class UserBehavior(TaskSet):

    def on_start(self):
        logger.info("on_start")
        self.auth_headers = load_test_util.getAuthHeaders()
        self.potato_queue = Queue()

    def on_stop(self):
        logger.info("on_stop, clear queue")

    def list_potato(self):
        self.client.get("/potato/api/v1/potatoes")

    @seq_task(1)
    def create_potato(self):
        post_dict = load_test_util.create_potato_request()

        post_data = json.dumps(post_dict)

        logger.info("auth_headers: %s", json.dumps(self.auth_headers))
        logger.info("post_data: %s", post_data)

        response = self.client.post("/potato/api/v1/potatoes", headers = self.auth_headers, data=post_data)
        logger.info("response: %d, %s", response.status_code, response.text)
        if (200 <= response.status_code < 300):
            potatoId = load_test_util.get_potato_id(response.text)
            logger.info("potatoId: %s" % potatoId)
            self.potato_queue.put(potatoId)

        return response

    @seq_task(2)
    def retrieve_potato(self):

        if not self.potato_queue.empty():
            potato_id = self.potato_queue.get(True, 1)
            logger.info("retrieve_potato by potato_id %s", potato_id)
            response = self.client.get("/potato/api/v1/potatoes/" + potato_id, headers=self.auth_headers)
            logger.info("retrieve_potato's response: %d, %s", response.status_code, response.text)
            self.potato_queue.put(potato_id)


    @seq_task(3)
    def update_potato(self):
         if not self.potato_queue.empty():
            potato_id = self.potato_queue.get(True, 1)
            post_dict = load_test_util.create_potato_request()

            put_data = json.dumps(post_dict)
            response = self.client.put("/potato/api/v1/potatoes", headers = self.auth_headers, data=put_data)
            logger.info("response: %d, %s", response.status_code, response.text)
            self.potato_queue.put(potato_id)

    @seq_task(4)
    def delete_potato(self):
        if not self.potato_queue.empty():
            potato_id = self.potato_queue.get(True, 1)
            response = self.client.delete("/potato/api/v1/potatoes/" + potato_id, headers = self.auth_headers)
            logger.info("response: %d, %s", response.status_code, response.text)

class WebsiteUser(HttpLocust):
    task_set = UserBehavior
    min_wait = 500
    max_wait = 3000