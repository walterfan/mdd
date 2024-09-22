#!/bin/bash
while getopts ":i:p:u:s:e:" opt; do
  case $opt in
    i) ip="$OPTARG"; ((count++));;
    p) port="$OPTARG"; ((count++));;
    u) user="$OPTARG"; ((count++));;
    s) password="$OPTARG"; ((count++));;
    \?) echo "Invalid option -$OPTARG" >&2; exit 1;;
  esac
done
if [[ $count != 4 ]]; then
  echo "error: please provide arguments for target server:\n  -i(host), -p(password), -u(username) and -s(secret)" >&2 && exit 1
fi

echo "Please run the script on a linux(ubunto) box"

cat > hosts <<EOF
[walter_site]
$ip

[walter_site:vars]
ansible_port=$port
ansible_user=$user
ansible_ssh_pass=$password
ansible_become_pass=$password
EOF

# replace ip adress with the input argument
sed -i "s|^KAFKA_CFG_ADVERTISED_LISTENERS=SASL_PLAINTEXT://[^:]*:|KAFKA_CFG_ADVERTISED_LISTENERS=SASL_PLAINTEXT://$ip:|g" .env
sed -i "s|^TELEGRAF_BROKERS=[^:]*:|TELEGRAF_BROKERS=$ip:|g" .env
sed -i "s|^TELEGRAF_URLS=http://[^:]*:|TELEGRAF_URLS=http://$ip:|g" .env

if ! command -v ansible &> /dev/null; then
  apt install ansible -y
fi
if ! command -v sshpass &> /dev/null; then
  apt install sshpass -y
fi

# replace configuration to ignore warning
# TODO: move sudo password
echo P@ss1234|sudo -S ls -l /etc/ansible
sudo sed -i 's/^#\(host_key_checking = False\)/\1/' /etc/ansible/ansible.cfg 
sudo sed -i 's/^#deprecation_warnings = True/deprecation_warnings = False/' /etc/ansible/ansible.cfg

# run ansible playbook
ansible-playbook -v -i hosts tig.yml 

