# Overview

TODO's pronuciation is "土豆" that is Potato in chinese.
So Potato is a TODO list like application.

# Potato Application

* PotatoWeb

* PotatoRegistry

* PotatoService

* TomatoService

* RemindService

* PotatoIdentity

# Application Framework

* Spring Boot

* Spring Cloud

# Quick Start
spring init --list

## registry

```
spring init --java-version=1.8 --dependencies=web,actuator,cloud-eureka-server,devtools -packaging=jar --groupId=com.github.walterfan.potato --artifactId=registry 

unzip registry.zip -d potato-registry
```

## common module

```
spring init --java-version=1.8 --dependencies= web,jpa -packaging=jar --groupId=com.github.walterfan.potato --artifactId=common

unzip common.zip -d potato-common
```

## server module

```
spring init --java-version=1.8 --dependencies=web,actuator,cloud-eureka,devtools -packaging=jar --groupId=com.github.walterfan.potato --artifactId=server

unzip server.zip -d potato-server

docker run -d --rm \
    --env JDBC_URL=jdbc:sqlite:/opt/potato/potato.db \
    -p 9003:9003 \
    -v `pwd`:/opt/potato \
    --name potato-server \
    walterfan/potato-app
```

## client module

```
spring init --java-version=1.8 --dependencies=web -packaging=jar --groupId=com.github.walterfan.potato --artifactId=client

unzip client.zip -d potato-client
```

## tomato module

```
spring init --java-version=1.8 --dependencies=web,actuator,cloud-eureka,devtools -packaging=jar --groupId=com.github.walterfan.potato --artifactId=tomato

unzip tomato.zip -d potato-tomato
```

## identity module

```
spring init --java-version=1.8 --dependencies=web,actuator,cloud-eureka,devtools -packaging=jar --groupId=com.github.walterfan.potato --artifactId=identity

unzip identity.zip -d potato-identity
```

## web module

```
spring init --java-version=1.8 --dependencies=web,actuator,devtools -packaging=jar --groupId=com.github.walterfan.potato --artifactId=web

unzip web.zip -d potato-web
```


spring init --java-version=1.8 --dependencies=web,actuator,devtools -packaging=jar --groupId=com.github.walterfan.potato --artifactId=trail


* run docker

```

docker tag walterfan/potato-app:0.0.1 walterfan/potato-app:latest

docker run --env JDBC_URL=jdbc:sqlite:/opt/potato/potato-server/sqlite/potato.db -d -p 9003:9003 --name potato-server walterfan/potato-app 

```

# docker compose usage

curl http://localhost:9002/scheduler/api/v1/ping

curl http://localhost:9003/potato/api/v1/ping 

# consul

```$xslt
docker run -d \
    -p 8500:8500 \
    -p 8600:8600/udp \
    --name=potato-consul \
    consul agent -server -ui -node=consul-server-1 \
    -bootstrap-expect=1 -client=0.0.0.0
```