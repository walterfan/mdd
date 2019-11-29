FROM java:8

MAINTAINER Walter Fan

VOLUME /tmp

RUN mkdir -p /opt

ADD ./target/task-0.0.1-SNAPSHOT.jar /opt/potato-app.jar

EXPOSE 9003

ENTRYPOINT ["java", "-jar", "/opt/potato-app.jar"]