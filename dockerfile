FROM openjdk:8-jdk-alpine
VOLUME /tmp

ENV JAVA_OPTS="-Xms128m -Xmx512m"

ADD target/a2si-facade.jar a2si-facade.jar


ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/a2si-facade.jar"]
