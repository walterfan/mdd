
# install ansible

# configuration

ansible all --list-hosts

ansible all -m ping

# install nginx
ansible-playbook nginx.yml

# install consul docker

docker run -d \
  -p 8500:8500 -p 8600:8600/udp \
  --name=potato-consul consul agent \
  -server -ui -node=consul-server-1 \
  -bootstrap-expect=1 -client=0.0.0.0

docker inspect potato-consul --format {{.NetworkSettings.Networks.bridge.IPAddress}}`: