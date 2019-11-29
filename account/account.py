import os
import json
import requests
import redis
from flask_httpauth import HTTPBasicAuth
from flask import make_response
from flask import Flask
from flask import request
from werkzeug.exceptions import NotFound, ServiceUnavailable
from flask import render_template

ACCOUNTS_API_PATH = "/api/v1/accounts"
REDIS_KEY = "walter_accounts"

app = Flask(__name__)

current_path = os.path.dirname(os.path.realpath(__file__))

auth = HTTPBasicAuth()

users = {
    "walter": "pass1234"
}

json_file = "{}/account.json".format(current_path)
redis_enabled = False
#docker run --restart always -p 6379:6379 -d --name local-redis redis

class RedisClient:
    def __init__(self):
        self.redis_host = "localhost"
        self.redis_port = 6379
        self.redis_password = ''
        self.redis_conn = None

    def connect(self):
        #if(redis_enabled):
        pool = redis.ConnectionPool(host=self.redis_host, port=self.redis_port)
        self.redis_conn = redis.Redis(connection_pool=pool)

    def set(self, key, value):
        self.redis_conn.set(key, value)

    def get(self, key):
        return self.redis_conn.get(key)

redis_client = RedisClient()

if(redis_enabled):
    redis_client.connect()

def read_data():
    if redis_enabled:
        jsonStr = redis_client.get(REDIS_KEY)
        if not jsonStr:
            jsonStr = "{}"
        return json.loads(jsonStr)
    else:
        json_fp = open(json_file, "r")
        return json.load(json_fp)


def save_data(accounts):
    if redis_enabled:
        redis_client.set(REDIS_KEY, json.dumps(accounts))
    else:
        json_fp = open(json_file, "w")
        json.dump(accounts, json_fp, sort_keys=True, indent=4)


@auth.get_password
def get_pw(username):
    if username in users:
        return users.get(username)
    return None


def generate_response(arg, response_code=200):
    response = make_response(json.dumps(arg, sort_keys=True, indent=4))
    response.headers['Content-type'] = "application/json"
    response.status_code = response_code
    return response


@app.route('/')
def index():
    return render_template('index.html')


@auth.login_required
@app.route(ACCOUNTS_API_PATH, methods=['GET'])
def list_account():
    accounts = read_data()
    return generate_response(accounts)


# Create account
@auth.login_required
@app.route(ACCOUNTS_API_PATH, methods=['POST'])
def create_account():
    account = request.json
    sitename = account["siteName"]
    accounts = read_data()
    if sitename in accounts:
        return generate_response({"error": "conflict"}, 409)
    accounts[sitename] = account
    save_data(accounts)
    return generate_response(account)


# Retrieve account
@auth.login_required
@app.route(ACCOUNTS_API_PATH + '/<sitename>', methods=['GET'])
def retrieve_account(sitename):
    accounts = read_data()
    if sitename not in accounts:
        return generate_response({"error": "not found"}, 404)

    return generate_response(accounts[sitename])


# Update account
@auth.login_required
@app.route(ACCOUNTS_API_PATH + '/<sitename>', methods=['PUT'])
def update_account(sitename):
    accounts = read_data()
    if sitename not in accounts:
        return generate_response({"error": "not found"}, 404)

    account = request.json
    print(account)
    accounts[sitename] = account
    save_data(accounts)
    return generate_response(account)


# Delete account
@auth.login_required
@app.route(ACCOUNTS_API_PATH + '/<sitename>', methods=['DELETE'])
def delete_account(sitename):
    accounts = read_data()
    if sitename not in accounts:
        return generate_response({"error": "not found"}, 404)

    del (accounts[sitename])
    save_data(accounts)
    return generate_response("", 204)


if __name__ == "__main__":
    app.run(port=5000, debug=True)

'''
http --auth walter:pass --json POST http://localhost:5000/api/v1/accounts \
    userName=walter password=pass siteName=163 siteUrl=http://163.com
'''