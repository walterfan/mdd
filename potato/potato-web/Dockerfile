FROM java:8

MAINTAINER Walter Fan

VOLUME /tmp

RUN mkdir -p /opt

ADD ./target/web-0.0.1-SNAPSHOT.jar /opt/potato-web.jar

EXPOSE 9005

ENTRYPOINT ["java", "-jar", "/opt/potato-web.jar"]
