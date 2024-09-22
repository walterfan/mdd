# Usage
为指定ip部署elk

## example
```sh
chmod +x elk.sh && sudo ./elk.sh -i 192.168.1.10 -p 22 -u <username> -s <password>
```

## jenkins build script

```sh

mkdir -p ~/.pip
cat <<EOF > ~/.pip/pip.conf
[global]
trusted-host =  mirrors.aliyun.com
index-url = http://mirrors.aliyun.com/pypi/simple
EOF
pip install --upgrade pip

virtualenv -p python3 venv
. ./venv/bin/activate
cd ./elk
chmod +x elk.sh
sudo ./elk.sh -i ${host} -p ${port} -u ${username} -s ${secret}
```