from locust import HttpLocust, TaskSet, task, seq_task

import load_test_util
import json
import yaml

from queue  import Queue
from threading import Timer
logger = load_test_util.init_logger("potato-web-test")
token_refresh_time = 300



class UserBehavior(TaskSet):

    def on_start(self):
        logger.info("on_start")
        self.auth_headers = load_test_util.getAuthHeaders()
        self.potato_queue = Queue()

    def on_stop(self):
        logger.info("on_stop, clear queue")

    @task
    def list_potato(self):
        self.client.get("/potatoes")

class WebsiteUser(HttpLocust):
    task_set = UserBehavior
    min_wait = 500
    max_wait = 3000