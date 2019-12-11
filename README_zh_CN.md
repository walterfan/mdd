# MDD 度量驱动开发

## 快速上手

依赖项

# modules

## Account
Account Service based on Flask

## Alertor
Alertor to check metrics and trigger alert based on ElasticSearch API.

## Potato
Potato service based on Spring boot

## Scripts
* Data Analysis scripts
* Performance testing scripts

## Docker

Some Dockerfiles

## oss

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