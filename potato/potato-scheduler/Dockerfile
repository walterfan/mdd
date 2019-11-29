FROM java:8

MAINTAINER Walter Fan

VOLUME /tmp

RUN mkdir -p /opt

ADD ./target/scheduler-0.0.1-SNAPSHOT.jar /opt/potato-scheduler.jar

EXPOSE 9002

ENTRYPOINT ["java", "-jar", "/opt/potato-scheduler.jar"]