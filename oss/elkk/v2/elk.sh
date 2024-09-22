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
[echo_pilot]
$ip

[echo_pilot:vars]
ansible_port=$port
ansible_user=$user
ansible_ssh_pass=$password
ansible_become_pass=$password
EOF

if ! command -v ansible &> /dev/null; then
    sudo apt-get update && sudo apt-get -y install ansible  
fi
if ! command -v sshpass &> /dev/null; then
    sudo apt-get update && sudo apt-get -y install sshpass 
fi

eval export $(cat .env)

sed -i 's/^#\(host_key_checking = False\)/\1/' /etc/ansible/ansible.cfg

sed -i "3s/.*/elasticsearch.hosts: [\"http:\/\/$ip:9200\"]/" kibana.yml
sed -i 's/bootstrap_servers => "[^"]*"/bootstrap_servers => "'"$ip"':9092"/' logstash.conf 
sed -i 's/hosts => \["http:\/\/[^"]*"/hosts => \["http:\/\/'"$ip"':9200"/' logstash.conf 
sed -i 's/password => "[^"]*"/password => "'"$ELASTIC_PASSWORD"'"/' logstash.conf

ansible-playbook -i hosts ansible-elk.yml


