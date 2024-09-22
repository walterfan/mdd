# MDD (度量驱动开发)

简体中文 | [English](REAME.md)

作者: Walter Fan [🤵](https://www.fanyamin.com)

[![Twitter Follow](https://img.shields.io/twitter/follow/walterfan?style=social)](https://twitter.com/walterfan)


## 概览
### 为什么选择MDD

* 如果你无法度量它，你就无法管理它。
* 如果你无法度量它，你就无法证明它。
* 如果你无法度量它，你就无法改进它。

### 什么是MDD

一种通过度量来推动开发，以不断改进软件产品和服务的开发方法：

* 基于度量来验证目标的达成。
* 基于度量来优化和提升系统。
* 基于度量来做决策。

### 度量内容

缩写 **USED**

- **Usage（使用量）:** 衡量用户与系统交互的频率及方式，反映功能参与度和活动水平。
- **Saturation（饱和度）:** 反映系统接近最大容量的程度，如CPU、内存或网络的使用情况。
- **Error（错误率）:** 追踪系统中故障或问题的发生，提供关于系统稳定性和可靠性的见解。
- **Delay（延迟）:** 衡量系统响应请求的时间，反映系统的性能和响应速度。

### 度量类型

* Gauge（仪表）: 某个时刻的瞬时值。
* Counter（计数器）: 递增和递减的值。
* Meter（计量器）: 一段时间内事件的平均发生率。
* Histogram（直方图）: 数据流中数值的统计分布。
* Timer（计时器）: 持续时间的直方图和调用次数的计量器。

## MDD 教程
* [Collected 的用法](oss/collectd/README.md)
* [ELKK 的用法](oss/elkk/readme.md) - ElasticSearch, LogStash, Kibana, Kafka
* [TIGK 的用法](oss/tig/v2/README.md) - Telegraf, InfluxDB, Grafana, Kafka
* [Hadoop 的用法](oss/hadoop/readme.md)
* [Prometheus 的用法](oss/prometheus/README.md)
* [如何模拟弱网条件 - 限流, 丢包, 延迟和抖动](https://www.jianshu.com/p/ce04bf2f9db6)
* [分析网络抓包用 python 更高效](https://www.jianshu.com/p/1a616442aaca)
* [WebRTC 之度量与统计: 到底出了什么问题](https://www.jianshu.com/p/419ca6fbdb60)
* [WebRTC 内部度量文件的分析](https://www.jianshu.com/p/efb933d55bba)
* [C++程序度量驱动调优实例：看狄更斯的双城记，寻找性能瓶颈](https://www.jianshu.com/p/a2988a17d146)
* [微服务缓存的使用度量](https://www.jianshu.com/p/35023210e637)
* [Redis 集群的构建和监控](https://www.jianshu.com/p/ced0a95cbc21)
* [JVM 参数怎么调](https://www.jianshu.com/p/20fb5ccffd9f)
* [使用 Redis 记录微服务的应用程序性能指数 APDEX](https://www.jianshu.com/p/684689942905)
* [线程池的监控与优化](https://www.jianshu.com/p/6b71221792fb)
* [内存溢出不可怕，手足无措才尴尬](https://www.jianshu.com/p/12d00ca68cda)
* [微服务日志分析之ELKK](https://www.jianshu.com/p/d391c63adcaa)
* [系统指标监控 collectd + influxDB + grafana](https://www.jianshu.com/p/e8c232228986)

## MDD 书籍

《微服务之道: 度量驱动开发》
 -- Walter Fan, Jian Fu

如果在安装和测试中遇到任何问题，请在此仓库中提交问题，我们将尽快回应并解决。

### 模块

1. 账户服务

	基于 Python 的 Flask 账户服务

2. 告警器

	基于 ElasticSearch API 监测度量并触发告警。

3. Potato 服务

	基于 Spring Boot 的 Potato 服务

   * Consul 快照:
   
	![Consul Snapshot](snapshot/consul_snapshot.png)

   * 网页:
   
	![Potato Service GUI](snapshot/potato_web_gui.png)

   * Web API:
   
	![Potato Service API](snapshot/potato_server_api.png)

4. 脚本

  * 数据分析脚本
  * 性能测试脚本
  
5. docker

	常用的 docker 文件

6. oss

	ELKK、TIG 等工具的安装和设置指南

7. devops

	开发运维相关脚本

### 环境设置

以 Ubuntu 16 为例

```
apt install docker
apt install docker-compose
```

#### Python 环境

```
apt install python3
apt install python3-pip

pip3 install virtualenv

virtualenv -p python3 venv
source venv/bin/activate

pip install fabric3
```

#### Java 环境

```
apt install openjdk-8-jdk
apt install maven
```

### 快速开始

请确保所有依赖已准备好。然后你可以依次启动并调试服务，使用 Consul 和 InfluxDB。

确保相关依赖已安装，并使用 docker-compose 来管理应用程序及其依赖项。如果你想按服务逐个调试，可以从 Consul 和 InfluxDB 开始。

```
cd potato
docker-compose start consul influxdb
```

potato/fabfile.py 文件包含构建和部署步骤，你可以首先运行它，然后逐步尝试各个步骤。

### Potato 服务示例

#### 依赖

 * python3
 * fabric3
 * jdk8
 * maven3

#### 部署

```
cd potato
fab redeploy
```

* 查看运行状态

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


* 打开 Potato 应用的 web 门户

```
open http://localhost:9005
```