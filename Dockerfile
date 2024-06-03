FROM maven:3.8.5-openjdk-17 as build

WORKDIR /app

COPY maven_repo /root/.m2/repository

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
