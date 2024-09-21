# Usage

向指定ip一键部署tig监控(kafka-telegraf-influxdb-grafana)

参数：

```
-i ip,
-p ssh_port, 
-u username, 
-s password
```

# run

```sh
chmod +x tig.sh && ./tig.sh -i 192.168.0.101 -p 22 -u <username> -s <password>
```