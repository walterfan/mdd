
# 1)	安装
•	ubuntu
```
apt update
apt install -y collectd collectd-utils
```

•	centos

```
yum install epel-release
yum install -y collectd
```

# 2)	配置
```
vi /etc/collectd/collectd.conf
```

配置中最主要的加载所需的丰富的 collectd 插件

```
#LoadPlugin logfile
LoadPlugin syslog

<Plugin syslog>
        LogLevel info
</Plugin>

##############################################################################
# LoadPlugin section                                                         #
#----------------------------------------------------------------------------#
# Specify what features to activate.                                         #
##############################################################################

LoadPlugin battery
LoadPlugin cpu

LoadPlugin df
LoadPlugin disk
#LoadPlugin dns
#LoadPlugin email
LoadPlugin entropy
LoadPlugin interface
LoadPlugin irq
#LoadPlugin java
#LoadPlugin libvirt
LoadPlugin load
LoadPlugin memory
<Plugin rrdtool>
        DataDir "/var/lib/collectd/rrd"
#       CacheTimeout 120
#       CacheFlush 900
#       WritesPerSecond 30
#       CreateFilesAsync false
#       RandomTimeout 0
#
# The following settings are rather advanced
# and should usually not be touched:
#       StepSize 10
#       HeartBeat 20
#       RRARows 1200
#       RRATimespan 158112000
#       XFF 0.1
</Plugin>
3)	启动
service collectd start
service collectd status
tail /var/log/syslog
```