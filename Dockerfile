FROM openjdk:8-jre-alpine
COPY /build/libs/graph-1.0.jar /usr/app/
WORKDIR /usr/app
ENTRYPOINT ["java", "-jar", "/usr/app/graph-1.0.jar", "-Darchaius.deployment.applicationId=graph", "-Darchaius.deployment.environment=prod"]