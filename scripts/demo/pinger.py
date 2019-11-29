import ssl
import json
import requests
import urllib3
import time

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

hostname = "10.224.77.179"
potato_web_url = 'http://%s:9005/api/v1/ping' % hostname
potato_service_url = 'http://%s:9003/potato/api/v1/ping' % hostname
potato_scheduler_url = 'http://%s:9002/scheduler/api/v1/ping' % hostname


potato_urls = [potato_web_url, potato_service_url, potato_scheduler_url]

def ping(url):
    print("# ping %s" % url)
    beginTime = time.time()
    resp = requests.get(url,  verify=False)
    endTime = time.time()
    json_data = resp.json()
    json.dumps(json_data, indent=4, sort_keys=True)

    serviceState = json_data['serviceState']

    if 'UP' != serviceState or 'up' != serviceState:

        for service in json_data['upstreamServices']:
            print("* %s : %s (%f s)" % (service['serviceName'], service['serviceState'], (endTime - beginTime)))
    else:
        print("* %s is %s (%f s)" % (url,  serviceState, (endTime - beginTime)))

    return serviceState




if __name__ == "__main__":
    for url in potato_urls:
        ping(url)
