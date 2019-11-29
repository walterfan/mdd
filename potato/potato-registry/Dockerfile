FROM java:8

VOLUME /tmp

ADD ./target/registry-0.0.1-SNAPSHOT.jar /potato-registry.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "/potato-registry.jar"]