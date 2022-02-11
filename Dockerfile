FROM openjdk:8-jre-alpine

RUN ./gradlew clean build jar

COPY ./build/libs/graph-1.0.jar /usr/app/

WORKDIR /usr/app

EXPOSE 7000

ENTRYPOINT ["java", "-jar", "graph-1.0.jar", "-Darchaius.deployment.applicationId=graph", "-Darchaius.deployment.environment=prod"]