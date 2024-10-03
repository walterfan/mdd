# Prometheus Monitoring Stack

This project is a simple monitoring stack based on docker compose. It is composed of:

* [Prometheus](https://prometheus.io/)
* [Grafana](https://grafana.com/)
* [Alertmanager](https://prometheus.io/docs/alerting/alertmanager/)
* [Node exporter](https://github.com/prometheus/node_exporter)
* [Cadvisor](https://github.com/google/cadvisor)

## Installation
### Node exporter

* example

```

./install_node_exporter.sh -i 192.168.1.10 -p 22 -u walter -s "***"
```

### Prometheus & Grafana

Project structure:
```
.
├── docker-compose.yaml
├── grafana
│   └── datasource.yml
├── prometheus
│   └── prometheus.yml
|-- alertmanager
|   └── alertmanager.yml
└── README.md
```

[_docker-compose.yaml_](docker-compose.yaml)
```
services:
  prometheus:
    image: prom/prometheus
    ...
    ports:
      - 9090:9090
  grafana:
    image: grafana/grafana
    ...
    ports:
      - 3000:3000

  alertmanager:
    image: prom/alertmanager

  cadvisor:
    image: google/cadvisor
    ...
```
The compose file defines a stack with services `prometheus` and `grafana`, `alertmanager` and `cadvisor`.
When deploying the stack, docker compose maps port the default ports for each service to the equivalent ports on the host in order to inspect easier the web interface of each service.
Make sure the ports 9090 and 3000 on the host are not already in use.

## Deploy with docker compose

```shell

$ docker-compose up -d
Creating network "prometheus-grafana_default" with the default driver
Creating volume "prometheus-grafana_prom_data" with default driver
...
Creating grafana    ... done
Creating prometheus ... done
Creating alertmanager ... done
Creating cadvisor ... done
Attaching to prometheus, grafana, alertmanager, cadvisor

$ docker-compose ps

    Name                  Command                  State                        Ports
-------------------------------------------------------------------------------------------------------
alertmanager   /bin/alertmanager --config ...   Up             0.0.0.0:9093->9093/tcp,:::9093->9093/tcp
cadvisor       /usr/bin/cadvisor -logtostderr   Up (healthy)   0.0.0.0:8080->8080/tcp,:::8080->8080/tcp
grafana        /run.sh                          Up             0.0.0.0:3000->3000/tcp,:::3000->3000/tcp
prometheus     /bin/prometheus --config.f ...   Up             0.0.0.0:9090->9090/tcp,:::9090->9090/tcp

```



## Expected result

Listing containers must show two containers running and the port mapping as below:
```
$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                    NAMES
dbdec637814f        prom/prometheus     "/bin/prometheus --c…"   8 minutes ago       Up 8 minutes        0.0.0.0:9090->9090/tcp   prometheus
79f667cb7dc2        grafana/grafana     "/run.sh"                8 minutes ago       Up 8 minutes        0.0.0.0:3000->3000/tcp   grafana
```

Navigate to `http://localhost:3000` in your web browser and use the login credentials specified in the compose file to access Grafana. It is already configured with prometheus as the default datasource.

Navigate to `http://localhost:9090` in your web browser to access directly the web interface of prometheus.

Stop and remove the containers. Use `-v` to remove the volumes if looking to erase all data.
```
$ docker compose down -v
```

## SSL certficate

* generate server key and cert in certs folder

```
mkdir certs
openssl genrsa -out ./certs/server.key 2048
openssl req -new -key ./certs/server.key -out ./ssl/server.csr
openssl x509 -req -days 365 -in ./certs/server.csr -signkey ./certs/server.key -out ./certs/server.crt
```