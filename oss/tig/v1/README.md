
# Telegraf

1) 创建安装源:
添加配置文件: /etc/yum.repos.d/influxdb.repo

```
[influxdb]
name = InfluxDB Repository - RHEL \$releasever
baseurl = https://repos.influxdata.com/rhel/\$releasever/\$basearch/stable
enabled = 1
gpgcheck = 1
gpgkey = https://repos.influxdata.com/influxdb.key
```

2)安装telegraf包：
```
sudo yum install telegraf
```

3) 启动
```
sudo service telegraf start
# 假设操作系统是CentOS 7+, RHEL 7+,使用下面systemd命令：
sudo systemctl start telegraf
```