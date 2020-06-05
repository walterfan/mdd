# MDD (Metrics Driven Development)

Samples for Metrics Driven Development Book
Any issues, please raise a git issue of this repo.


如何在安装和测试中有任何问题，请在本仓库中提交问题，将会尽快回应和解决.

## modules

1. Account

	Account Service based on Flask in Python

2. Alertor

	Alertor to check metrics and trigger alert based on ElasticSearch API.

3. Potato

	Potato service based on Spring boot

   * Consul snapshot:
   
	![Consul Snapshot](snapshot/consul_snapshot.png)

   * Web Page:
   
	![Potato Service GUI](snapshot/potato_web_gui.png)

   * Web API:
   
	![Potato Service API](snapshot/potato_server_api.png)

4. Scripts

  * Data Analysis scripts
  * Performance testing scripts
  
5. docker

	Commonly used docker files

6. oss

	The installation and setup guideline of ELKK, TIG, etc.

7. devops

	The devops scripts


# Environments

Take Ubuntu 16 as example


```
apt install docker
apt install docker-compose

```
## python environment

```
apt install python3
apt install python3-pip

pip3 install virtualenv

virtualenv -p python3 venv
source venv/bin/activate

pip install fabric3
```

## Java environment

```
apt install openjdk-8-jdk
apt install maven
```

# Quick start

Please make sure the dependencies are ready.
And you can start and debug the service one by one with consul and influxdb.

请确保相关的依赖已经安装就绪， 这里使用 docker compose 来管理应用和其依赖组件。
如果想逐个服务调试，可以先只启动 consule 和 influxdb

```
cd potato
docker-compose start consul influxdb
```

The fabric file (potao/fabfile.py) contains the building and deployment steps, so you can try it firstly, then try every stesp by yourself.


## potato service


### Dependencies
 * python3
 * fabric3
 * jdk8
 * maven3
 


* deployment

```
cd potato
fab redeploy
```

* check running status

```

docker-compose ps
                Name                                   Command                                  State                                   Ports
-------------------------------------------------------------------------------------------------------------------------------------------------------------
consul                                  docker-entrypoint.sh agent ...          Up                                      8300/tcp, 8301/tcp, 8301/udp,
                                                                                                                        8302/tcp, 8302/udp,
                                                                                                                        0.0.0.0:8400->8400/tcp,
                                                                                                                        0.0.0.0:8500->8500/tcp,
                                                                                                                        0.0.0.0:8600->8600/tcp,
                                                                                                                        0.0.0.0:8600->8600/udp
influxdb                                /entrypoint.sh influxd                  Up                                      0.0.0.0:8083->8083/tcp,
                                                                                                                        0.0.0.0:8086->8086/tcp
local-mysql                             docker-entrypoint.sh --ini ...          Up                                      0.0.0.0:3306->3306/tcp, 33060/tcp
potato-scheduler                        java -jar /opt/potato-sche ...          Up                                      0.0.0.0:9002->9002/tcp
potato-server                           java -jar /opt/potato-app.jar           Up                                      0.0.0.0:9003->9003/tcp
potato-web                              java -jar /opt/potato-web.jar           Up                                      0.0.0.0:9005->9005/tcp
potato-zipkin                           /busybox/sh run.sh                      Up                                      9410/tcp, 0.0.0.0:9411->9411/tcp   
```

* Open the web portal of potato application

```
open http://localhost:9005
```