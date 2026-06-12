ARG DOCKER_REGISTRY=docker.m.daocloud.io/

FROM ${DOCKER_REGISTRY}library/maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY settings.xml /root/.m2/settings.xml

COPY pom.xml .

RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src

RUN mvn -B package -DskipTests

FROM ${DOCKER_REGISTRY}library/eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "-Duser.timezone=GMT+08", "app.jar"]
