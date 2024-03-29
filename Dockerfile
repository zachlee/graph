FROM openjdk:8
COPY ./build/libs/graph-1.0-all.jar app/
EXPOSE 80
CMD ["java", "-Darchaius.deployment.applicationId=graph", "-Darchaius.deployment.environment=test", "-jar", "/app/graph-1.0-all.jar"]