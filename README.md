# MDD (Metrics Driven Development)

Samples for Metrics Driven Development Book


## modules

1. Account

Account Service based on Flask in Python

2. Alertor

Alertor to check metrics and trigger alert based on ElasticSearch API.

3. Potato

Potato service based on Spring boot

4. Scripts

* Data Analysis scripts
* Performance testing scripts

5. Docker

Some Dockerfiles

6. oss

The installation and setup guideline of ELKK, TIG, etc.

## devops

The devops scripts

# Environments

Take Ubuntu 16 as example

```
apt install docker
apt install docker-compose

```
## python environment

```
apt install python3
apt install python3-pip

pip3 install virtualenv

virtualenv -p python3 venv
source venv/bin/activate

pip install fabric3
```

## Java environment

```
apt install openjdk-8-jdk
apt install maven
```

# Quick start

## potato

* test

```
open http://localhost:9005
```

* deployment

```
cd potato
fab redeploy

```
