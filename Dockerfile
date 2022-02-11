FROM openjdk:8
COPY ./build/libs/graph-1.0-all.jar app/
EXPOSE 7000
CMD ["java", "-jar", "/app/graph-1.0-all.jar", "-Darchaius.deployment.applicationId=graph", "-Darchaius.deployment.environment=prod"]