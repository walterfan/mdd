

# LogStash 

1）下载解压

```
wget https://artifacts.elastic.co/downloads/logstash/logstash-7.3.1.tar.gz
tar xvfz logstash-7.3.1.tar.gz
mv logstash-7.3.1 /opt/logstash
```

2)  配置
```
cd /opt/logstash/
vi logstash.conf

```
* 注：配置文件相当复杂一点，参见
  * 前端: logstash/logstash_agent.conf
  * 后端: logstash/logstash_server.conf


3） 启动
./bin/logstash -f logstash.conf &


# ElasticSearch 

1) 下载解压
```
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.2.4.tar.gz

tar xvfz elasticsearch-6.2.4.tar.gz
```

2）配置

```
cd elasticsearch-6.2.4

sed -i 's/#network.host: 192.168.0.1/network.host: 10.224.77.184/g'  ./config/elasticsearch.yml 
```

新版本的 Elasticsearch 不允许由 root 用户启动，并且对文件句柄打开个数有所要求，所以要做些改动

```
ulimit -n 262144
sysctl -w vm.max_map_count=262144

groupadd elsearch

useradd elsearch -g elsearch -p elsearch

chown -R elsearch:elsearch /opt/elasticsearch
su elsearch 
```
3） 以后台方式启动

```
cd /opt/elasticsearch
./bin/elasticsearch -d

```

可以快速查看一下状态

```
curl 'http://10.224.77.184:9200/?pretty'

```

# Kibana

1）下载解压
```
wget https://artifacts.elastic.co/downloads/kibana/kibana-7.4.2-linux-x86_64.tar.gz
tar xvfz kibana-7.4.2-linux-x86_64.tar.gz
mv kibana-7.4.2-linux-x86_64 /opt/kibana
```

2）配置
```
$ cd /opt/kibana
$ vi config/kibana.yml
------------------------
server.host: "10.224.77.175"
elasticsearch.hosts: ["http://10.224.77.175:9200"]
```

3）启动

```
./kibana --allow-root &
```

# Kafka

## 1.  Zookeeper安装步骤

1）下载解压
```
wget http://apache.org/dist/zookeeper/stable/apache-zookeeper-3.5.5-bin.tar.gz
tar xvfz http://apache.org/dist/zookeeper/stable/apache-zookeeper-3.5.5-bin.tar.gz
```

2）创建相关目录和配置

```
mkdir -p /opt

mv apache-zookeeper-3.5.5-bin /opt/zookeeper
cd /opt/zookeeper/conf
cp zoo_sample.cfg zoo.cfg
mkdir -p /opt/data/zookeeper
vi zoo.cfg
```

在 zoo.cfg 文件将数据目录修改为： 

```
dataDir=/opt/zookeeper/data 
```

3）启动
```
cd zookeeper/bin/
./zkServer.sh start
```

## 2．Kafka 安装步骤
1) 下载解压
```
wget http://apache.claz.org/kafka/2.3.0/kafka_2.12-2.3.0.tgz
tar xvfz kafka_2.12-2.3.0.tgz
```

2) 创建相关目录和修改配置
```
mv kafka_2.12-2.3.0 /opt/kafka
cd /opt/kafka/
vi config/server.properties
```

修改配置文件 config/server.properties如下

```
mkdir -p /opt/kafka/logs
log.dirs=/opt/kafka/logs
listeners = PLAINTEXT://10.224.77.178:9092
#broker.id=3
#实际生产环境 Kakfa 和 zookeepr 不可能放在一台主机上，并且各自要有多台来保证高可用性
zookeeper.connect=10.227.77.1781:2181
```

3) 启动 Kafka
```
./bin/kafka-server-start.sh -daemon config/server.properties
```

4）验证一下
启动后，即可使用命令行或者Kafka manager来操作Kafka，例如使用命令行来创建一个测试主题

```
./bin/kafka-topics.sh -create -zookeeper localhost:2181 --replication-factor 1 -partitions 1 -topic mlogs
```

  获取这个测试主题

```
./kafka-topics.sh --bootstrap-server 10.224.77.178:9092 --list
```
