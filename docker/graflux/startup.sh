service influxdb start
service grafana-server start

influx -execute 'CREATE DATABASE metrics WITH DURATION 6h REPLICATION 1 NAME "default"'

while true; do sleep 3600; done
