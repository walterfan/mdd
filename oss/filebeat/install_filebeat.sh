#!/bin/bash
while getopts ":i:p:u:s:e:" opt; do
  case $opt in
    i) ip="$OPTARG"; ((count++));;
    p) port="$OPTARG"; ((count++));;
    u) user="$OPTARG"; ((count++));;
    s) password="$OPTARG"; ((count++));;
    e) environment="$OPTARG"; ((count++));;
    \?) echo "Invalid option -$OPTARG" >&2; exit 1;;
  esac
done
if [[ $count != 5 ]]; then
  echo "Failed: please include -i, -p, -u, -s, -e." >&2 && exit 1
fi
cat > hosts <<EOF
[echo_pilot]
$ip

[echo_pilot:vars]

ansible_port=$port
ansible_user=$user
ansible_ssh_pass=$password
ansible_become_pass=$password

ip=$ip
port_num=$port
username=$user
password=$password
env=$environment
EOF

if ! command -v ansible &> /dev/null; then
    apt install ansible 
fi
if ! command -v sshpass &> /dev/null; then
    apt install sshpass 
fi

sudo sed -i 's/^#\(host_key_checking = False\)/\1/' /etc/ansible/ansible.cfg
chmod +x filebeat.sh
ansible-playbook -i hosts ansible-filebeat.yml


