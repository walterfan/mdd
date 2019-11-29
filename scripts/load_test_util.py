import os
import sys
import logging
import json
import base64
import uuid
import yaml
import requests
from pytz import timezone
from datetime import datetime, timedelta
import random
import string

DATE_FORMAT = '%Y-%m-%dT%H:%M:%S%z'

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

logger = init_logger(__name__)
test_potato_request_str = None

def randomString(nLen=8):
    """Generate a random string of fixed length """
    letters = string.ascii_lowercase
    return ''.join(random.choice(letters) for i in range(nLen))

class YamlConfig:
    def __init__(self, yaml_file):
        self.config_file = yaml_file
        self.config_data = self.read_config(yaml_file)

    def read_config(self, yaml_file):
        logger.debug("Open file", yaml_file)
        f = open(yaml_file, 'r', encoding='UTF-8')
        logger.debug("Loading file", yaml_file)
        config_data = yaml.load(f, Loader=yaml.FullLoader)
        logger.debug("Loaded file", yaml_file)
        f.close()
        logger.debug("Close file", yaml_file)
        return config_data

    def get_config(self):
        return self.config_data

    def __str__(self):
        return yaml.dump(self.config_data, Dumper=yaml.Dumper)


class AuthConfig:

    def __init__(self, configDict):

        self.clientId = configDict.get('clientId')
        self.clientSecret = configDict.get('clientSecret')



def getAuthHeaders(env='lab', tokenPrefix = 'Basic'):

    ymlConfig = YamlConfig('./load_test_config.yml')
    authConfig = AuthConfig(ymlConfig.get_config().get(env))

    data = authConfig.clientId + ':' + authConfig.clientSecret

    encoded_data = base64.b64encode(data.encode())
    encoded_str = tokenPrefix + " " + encoded_data.decode()


    get_headers = {}
    get_headers["Content-Type"] = "application/json"
    get_headers["Authorization"] = tokenPrefix + encoded_str
    return get_headers


def create_account_request(siteName=None, siteUrl=None, userName='walter', password="pass"):
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

def create_potato_request(name='read book', desc='summarize book'):

    json_file = './potato_request.json'
    json_content = test_potato_request_str

    if not json_content:
        with open(json_file, "r") as fp:
            json_content = fp.read()
    
    
    jsonDict = json.loads(json_content)
    
    #jsonDict['name'] = name
    #jsonDict['description'] = desc

    today = datetime.now(timezone('UTC'))
    #jsonDict['scheduleTime'] = today.strftime(DATE_FORMAT)

    return jsonDict

def get_potato_id(response_text):
    resp = json.loads(response_text)
    potato_id = None
    if(resp):
        potato_id = resp.get("id")
        logger.info("potato_id is %s", potato_id)

    return potato_id


if __name__ == "__main__":
    post_header = getAuthHeaders()
    post_data = create_potato_request()
    post_data['id'] = str(uuid.uuid4())
    post_text = json.dumps(post_data)
    logger.info("post_text: %s", post_text);
    potato_id = get_potato_id(post_text)
    logger.info("potato_id: %s", potato_id);
    url = 'http://localhost:9003/potato/api/v1/potatoes'
    try:
        response = requests.post(url,
                                data=post_text,
                                headers=post_header,
                                verify=False)
        logger.info("response status_code: %d, response text: %s." % (response.status_code, response.text))
    except requests.RequestException as e:
        print(e, url)
        

