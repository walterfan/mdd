# Getting Started


### Build docker images

```
docker build -t walterfan/potato-app:0.0.1 .
docker run -d -p 9003:9003 walterfan/potato-app:0.0.1
docker push walterfan/potato-app:0.0.1
docker tag walterfan/potato-app:0.0.1 walterfan/potato-app:latest

docker run -d -p 9003:9003 --name potato-server walterfan/potato-app --env JDBC_URL=jdbc:sqlite:~/Documents/potato.db

```


### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

