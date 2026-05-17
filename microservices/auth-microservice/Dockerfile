FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl microservices/auth-microservice -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/microservices/auth-microservice/target/auth-microservice-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8096
ENTRYPOINT ["java", "-jar", "app.jar"]
