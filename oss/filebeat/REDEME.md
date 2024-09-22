# Usage
对指定ip安装filbeat

## example

```sh
chmod +x install_filebeat.sh 
sudo ./install_filebeat.sh -i 192.168.1.10 -p 22 -u walter -s P@ss1234 -e dev

```

## parameters

* -i echo box's ip to deploy
* -p ssh port
* -u ssh username
* -s ssh password
* -e environment(dev, qa, staging or prod)

## notes

should set the following configuration items in filebeat.yml

```
# avoid too many open files
close_inactive: 5m
# avoid too high cpu
scan_frequency: 2m
```