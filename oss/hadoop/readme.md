
# deployment mode

* 单机

* 单机伪分布

* 集群分布

# Installation

vi /etc/hosts

```

127.0.0.1       localhost

127.0.1.1       localhost.localdomain localhost
10.224.77.175   wdm1.qa.webex.com wdm1
10.224.77.178   wdm2.qa.webex.com wdm2
10.224.77.179   wdm3.qa.webex.com wdm3

```

* Install JDK
set JAVA_HOME


* setup user and group

```
groupadd hadoop-user
useradd -g hadoop-user hadoop
passwd hadoop
```

* setup ssh 

```
ssh-keygen -t rsa
cp ~/.ssh/id_rsa.pub ./authorized_keys
```

* download hadoop

```
wget http://apache.website-solution.net/hadoop/common/hadoop-2.8.4/hadoop-2.8.4.tar.gz
tar -xvfz hadoop-2.8.4.tar.gz
```

* 初始化 hadoop

```
1）格式化namenode

$ bin/hdfs namenode –format
2）启动NameNode 和 DataNode 守护进程

$ sbin/start-dfs.sh
3）启动ResourceManager 和 NodeManager 守护进程

$ sbin/start-yarn.sh
5，启动验证 
1）执行jps命令，有如下进程，说明Hadoop正常启动

# jps
6097 NodeManager
11044 Jps
7497 -- process information unavailable
8256 Worker
5999 ResourceManager
5122 SecondaryNameNode
8106 Master
4836 NameNode
4957 DataNode
```

# Reference
ansible-galaxy install indigo-dc.hadoop