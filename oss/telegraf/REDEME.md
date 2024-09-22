# Usage

向指定ip部署telegraf，并把收集到的信息发到云端的kafka

## example

```sh
sudo chmod +x install_telegraf.sh 
sudo ./install_telegraf.sh -i 192.168.1.10 -p 22 -u walter -s P@ss1234 -e qa
```

## parameters

* -i echo box's ip to deploy
* -p ssh port
* -u ssh username
* -s ssh password
* -e environment(dev, qa, staging or prod)

