# Getting Started


### Build docker images

```
docker build -t walterfan/potato-app:0.0.1 .
docker run -d -p 9003:9003 walterfan/potato-app:0.0.1
docker push walterfan/potato-app:0.0.1
docker tag walterfan/potato-app:0.0.1 walterfan/potato-app:latest

docker run -d -p 9003:9003 --name potato-server walterfan/potato-app --env JDBC_URL=jdbc:sqlite:~/Documents/potato.db

```


### MySQL prepare


```
mysql -u root -p

mysql> create database potato  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; 

mysql> create user 'walter'@'%' identified by 'pass1234'; 

mysql> grant all on potato.* to 'walter'@'%'; 

```


