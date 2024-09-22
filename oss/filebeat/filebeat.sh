#!/bin/bash
if ! systemctl list-unit-files | grep -q "filebeat.service"; then
    wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -
    apt install apt-transport-https
    echo "deb https://artifacts.elastic.co/packages/8.x/apt stable main" | sudo tee -a /etc/apt/sources.list.d/elastic-8.x.list
    apt update && apt install filebeat
fi

#device_id=$(cat /sys/firmware/devicetree/base/serial-number)
#sed -i "s/device_id:.*\d*/device_id: $device_id/" /home/walter/deploy/filebeat/filebeat.yml
mv -f /home/walter/deploy/filebeat/filebeat.yml /etc/filebeat/filebeat.yml 
chmod 644 /etc/filebeat/filebeat.yml 
chown root /etc/filebeat/filebeat.yml 
systemctl enable filebeat && systemctl restart filebeat

