1. start prometheus, cadvisor, grafana and potato-web

* please install docker and docker-compose firstly

```
mkdir -p ./data/grafana
docker-compose up -d
docker-compose ps
```

2. open the following address by browser 

suppose your start them on your computer - localhost

* prometheus: http://localhost:9090
* cadvisor: http://localhost:8020
* potato-web: http://localhost:9005
* grafana: http://localhost:3000

3. stop  prometheus, cadvisor, grafana and potato-web

```
docker-compose down
```
