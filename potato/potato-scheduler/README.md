# quick start

## scheduler

```$xslt
docker-compose up mysqldb
docker-compose up scheduler
```

links:

* http://localhost:9002/actuator
* http://localhost:9002/swagger-ui.html
* http://localhost:9002/scheduler/api/v1/ping

# setup mysql 


```
cp src/main/resource/schema.sql ../data/db/mysql

mysql -u root -p

mysql> create database scheduler  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; 

mysql> create user 'walter'@'%' identified by 'pass1234'; 

mysql> grant all on scheduler.* to 'walter'@'%'; 

mysql> use scheduler

mysql> source /var/lib/mysql/schema.sql

```

grant all on userservice.* to 'walter'@'%'

# docker build

```

docker build -t walterfan/potato-scheduler:0.0.1 .

docker run -d -p 9002:9002 walterfan/potato-scheduler:0.0.1

docker push walterfan/potato-scheduler:0.0.1

docker tag walterfan/potato-scheduler:0.0.1 walterfan/potato-scheduler:latest
```



# send a test email

```
curl -v -H "Content-Type:application/json" -X POST -d@./src/test/resources/email_task.json http://localhost:9002/scheduler/api/v1/reminders

```

Note that, Gmail’s SMTP access is disabled by default. To allow this app to send emails using your Gmail account -

* Go to https://myaccount.google.com/security?pli=1#connectedapps
* Set ‘Allow less secure apps’ to YES


# FAQ

## Table 'scheduler.QRTZ_LOCKS' doesn't exist

docker-compose stop mysqldb

rm -rf data/db/mysql/*

fab mysql_cli

create database scheduler  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; 

use scheduler

docker-compose restart scheduler

## Potato-scheduler startup failure

```
fab mysql_cli
mysql> grant all on scheduler.* to 'walter'@'%'; 
quit

docker-compose up -d scheduler
```
# reference
* https://www.callicoder.com/spring-boot-quartz-scheduler-email-scheduling-example/