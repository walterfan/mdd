#!/bin/bash
while getopts ":i:p:u:s:" opt; do
  case $opt in
    i) ip="$OPTARG"; ((count++));;
    p) port="$OPTARG"; ((count++));;
    u) user="$OPTARG"; ((count++));;
    s) password="$OPTARG"; ((count++));;
    \?) echo "Invalid option -$OPTARG" >&2; exit 1;;
  esac
done
if [[ $count != 4 ]]; then
  echo "Failed: please include -i, -p, -u, -s." >&2 && exit 1
fi
cat > hosts <<EOF
[node_exporter_hosts]
$ip

[node_exporter_hosts:vars]
ansible_port=$port
ansible_user=$user
ansible_ssh_pass=$password
ansible_become_pass=$password

node_exporter_username=prometheus
node_exporter_group=prometheus
node_exporter_password=walter1@34

EOF

if ! command -v ansible &> /dev/null; then
    sudo apt-get update && sudo apt-get -y install ansible
fi
if ! command -v sshpass &> /dev/null; then
    sudo apt-get update && sudo apt-get -y install sshpass
fi
export ANSIBLE_HOST_KEY_CHECKING=False
ansible-playbook -i hosts install_node_exporter.yaml


