# Overview

TODO's pronuciation is "土豆" that is Potato in chinese.
So Potato is a TODO list like application.

# Potato Application

* PotatoWeb: Web server of potato application

* PotatoService: API server of potato application

* RemindService: Remind server of potato application

there servers are registered to consul or eureka according to configuration

# Application Framework

* Spring Boot

* Spring Cloud

# Quick Start

* set the correct environment variables: 

for example

```
export EMAIL_SMTP_SERVER=smtp.gmail.com
export EMAIL_USER=xxx@gmail.com
export EMAIL_PWD=pass1234
export MYSQL_PWD=pass1234
export MYSQL_URL="jdbc:mysql://mysqldb/scheduler?useUnicode=true&characterEncoding=utf8"
export MYSQL_USER=potato

```

* run all services

```

source setenv.sh
docker-compose up -d
```

* only run consul

```
source setenv.sh
docker-compose up -d consul
```
