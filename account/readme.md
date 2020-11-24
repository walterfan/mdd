# Steps 步骤


1) 先安装 Libev 和 Python3，这是这个微服务所依赖的运行环境和库

```
brew install libev
brew install python3
```

2) 再安装 virtualenv 和 所需要的类库, 步骤如下

```
virtualenv pip3 install virtualenv
virtualenv -p python3 venv
source venv/bin/activate
# then install the required libraries
pip install -r requirements.txt
```

所需类库在 requirements.txt 描述如下

```
flask
flask-httpauth
requests
httpie
redis
locust
```
3) 然后启动服务

```
python account_service.py
```

让我们来对这个微服务做一个简单的测试，可使用 httpie （参见 [https://httpie.org/](https://httpie.org/)） 来发送 HTTP 请求，

假设我们在网易和微博站点上都有自己的帐号，我们可以调用它的 API 进行帐号添加：

* 添加网易帐号

```
http --auth walter:pass --json POST http://localhost:5000/api/v1/accounts userName=walter password=pass siteName=163 siteUrl=http://163.com
# 收到的响应:
HTTP/1.0 200 OK
Content-Length: 108
Content-type: application/json
Date: Thu, 24 Oct 2019 14:08:05 GMT
Server: Werkzeug/0.12.2 Python/3.7.3

{
    "password": "pass",
    "siteName": "163",
    "siteUrl": "http://163.com",
    "userName": "walter"
}
```

* 添加微博帐号
```
http --auth walter:pass --json POST http://localhost:5000/api/v1/accounts userName=walter password=pass siteName=weibo siteUrl=http://weibo.com
# 收到的响应:
HTTP/1.0 200 OK
Content-Length: 108
Content-type: application/json
Date: Thu, 24 Oct 2019 14:08:05 GMT
Server: Werkzeug/0.12.2 Python/3.7.3

{
    "password": "pass",
    "siteName": "weibo",
    "siteUrl": "http://weibo.com",
    "userName": "walter"
}
```

* 获取所有帐号

```
http --auth walter:pass --json GET http://localhost:5000/api/v1/accounts
# 收到的响应:
HTTP/1.0 200 OK
Content-Length: 290
Content-type: application/json
Date: Thu, 24 Oct 2019 14:20:54 GMT
Server: Werkzeug/0.12.2 Python/3.7.3

{
    "163": {
        "password": "pass",
        "siteName": "163",
        "siteUrl": "http://163.com",
        "userName": "walter"
    },
    "weibo": {
        "password": "pass",
        "siteName": "weibo",
        "siteUrl": "http://weibo.com",
        "userName": "walter"
    }
}

```

