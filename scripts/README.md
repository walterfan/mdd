# Load testing by Locust

## Quick start

* please install libev and python3.6 firstly

```
brew install libev
brew install python3
virtualenv pip3 install virtualenv
# then install the required libraries

virtualenv -p python3 venv
source venv/bin/activate

```

* then install the required libraries

```
pip install -r requirements.txt

locust -f account_load_test.py --host=http://localhost:5000

locust -f potato_web_test.py --host=http://localhost:9005

locust -f potato_load_test.py --host=http://localhost:9003

```


* no web method
```
locust --f potato_load_test.py --host=http://localhost:9003 --no-web -c 1 -r 1 -t 1m --print-stats

locust -f potato_load_test.py --host=http://localhost:9003 --no-web -c 10 -r 2 -t 1m
```