# MDD (Metrics Driven Development)

English | [简体中文](README_zh_CN.md)

Author: Walter Fan [🤵](https://www.fanyamin.com)

[![Twitter Follow](https://img.shields.io/twitter/follow/walterfan?style=social)](https://twitter.com/walterfan)


## Overview
### Why MDD

* If you can’t measure it, you can’t manage it
* If you can’t measure it, you can’t prove it
* If you can’t measure it, you can’t improve it

### What's MDD

A development approach driven by metrics to continuously improve software products and services:

* Build metrics into design, coding and DevOps from the beginning.
* Validate the achievement of goals, optimize, enhance and make decision based on metrics.
* Execute PDCA(Plan, Do, Check, Act) according to metrics.


### Metrics Content

**USED**

- **Usage:** Measures how frequently and in what ways users interact with the system, indicating feature engagement and activity levels.
- **Saturation:** Reflects how close the system is to its maximum capacity, such as CPU, memory, or network usage.
- **Error:** Tracks the occurrence of failures or issues within the system, providing insights into stability and reliability.
- **Delay:** Measures the time it takes for the system to respond to requests, reflecting its performance and responsiveness.

### Metrics Type

* Gauge: The instantaneous value of something.
* Counter: An incrementing and decrementing value.
* Meter: The average rate of events over a period of time.
* Histogram: The statistical distribution of values in a stream of data.
* Timer: A histogram of durations and a meter of calls.


## MDD Tutorial

* [Collected Usage](oss/collectd/README.md)
* [ELK Usage](oss/elkk/v2/readme.md) - ElasticSearch, LogStash/[Filebeat](oss/filebeat/REDEME.md), Kibana
* [TIGK Usage](oss/tig/v2/README.md) - [Telegraf](oss/telegraf/REDEME.md), InfluxDB, Grafana, Kafka
* [Hadoop Usage](oss/hadoop/readme.md)
* [Prometheus Usage](oss/prometheus/v2/README.md): Promethues + Grafana
* [如何模拟弱网条件 - 限流, 丢包, 延迟和抖动](https://www.jianshu.com/p/ce04bf2f9db6)
* [分析网络抓包用 python 更高效](https://www.jianshu.com/p/1a616442aaca)
* [WebRTC 之度量与统计: 到底出了什么问题](https://www.jianshu.com/p/419ca6fbdb60)
* [WebRTC 内部度量文件的分析](https://www.jianshu.com/p/efb933d55bba)
* [C++程序度量驱动调优实例：看狄更斯的双城记，寻找性能瓶颈](https://www.jianshu.com/p/a2988a17d146)
* [微服务缓存的使用度量](https://www.jianshu.com/p/35023210e637)
* [Redis 集群的构建和监控](https://www.jianshu.com/p/ced0a95cbc21) - [script](oss/redis/README.md)
* [JVM 参数怎么调](https://www.jianshu.com/p/20fb5ccffd9f)
* [使用 Redis 记录微服务的应用程序性能指数 APDEX](https://www.jianshu.com/p/684689942905)
* [线程池的监控与优化](https://www.jianshu.com/p/6b71221792fb)
* [内存溢出不可怕，手足无措才尴尬](https://www.jianshu.com/p/12d00ca68cda)
* [微服务日志分析之ELKK](https://www.jianshu.com/p/d391c63adcaa)
* [系统指标监控 collectd + influxDB + grafana](https://www.jianshu.com/p/e8c232228986)

## MDD Book

"The Way of Microservices: Metrics-Driven Development"
 -- Walter Fan, Jian Fu

If you encounter any issues during installation or testing, please submit an issue in this repository, and it will be addressed and resolved as soon as possible.

### modules

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


### Environments

Take Ubuntu 16 as example


```
apt install docker
apt install docker-compose

```
#### python environment

```
apt install python3
apt install python3-pip

pip3 install virtualenv

virtualenv -p python3 venv
source venv/bin/activate

pip install fabric3
```

#### Java environment

```
apt install openjdk-8-jdk
apt install maven
```

### Quick start

Please make sure the dependencies are ready.
And you can start and debug the service one by one with consul and influxdb.

≈
Make sure that the relevant dependencies are installed, and that Doccompos is used to manage the application and its dependencies.
If you want to debug on a service-by-service basis, you can start with just Consour and Inflix Deb

```
cd potato
docker-compose start consul influxdb
```

The fabric file (potao/fabfile.py) contains the building and deployment steps, so you can try it firstly, then try every stesp by yourself.


### potato service example


#### Dependencies
 * python3
 * fabric3
 * jdk8
 * maven3



#### deployment

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
