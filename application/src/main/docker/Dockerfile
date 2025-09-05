
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY . .

RUN ls -al && mvn -X clean package -DskipTests

FROM registry.access.redhat.com/ubi9/openjdk-21:1.21

ENV LANGUAGE='en_US:en'
ENV LANG='en_US.UTF-8'
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0"

USER 185
WORKDIR /deployments

COPY --from=builder /app/application/target/quarkus-app/ .

EXPOSE 8081
CMD ["java", "-jar", "quarkus-run.jar"]