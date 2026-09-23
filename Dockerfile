# syntax=docker/dockerfile:1

# Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw org.apache.maven.plugins:maven-dependency-plugin:3.8.1:resolve \
    org.apache.maven.plugins:maven-dependency-plugin:3.8.1:resolve-plugins

COPY src/ src/
RUN ./mvnw -o clean package -Dmaven.test.skip=true

RUN java -Djarmode=tools -jar target/*.jar extract --layers --destination /workspace/extracted
# Nome fixo (sem versão) para o ENTRYPOINT não depender do <version> do pom.xml.
RUN mv /workspace/extracted/application/*.jar /workspace/extracted/application/app.jar

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S cartoes && adduser -S cartoes -G cartoes

COPY --from=build --chown=cartoes:cartoes /workspace/extracted/dependencies/ ./
COPY --from=build --chown=cartoes:cartoes /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=cartoes:cartoes /workspace/extracted/application/ ./

USER cartoes

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
