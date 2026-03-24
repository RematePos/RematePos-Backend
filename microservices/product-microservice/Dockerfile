FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN  apk add --no-cache maven
COPY . .
RUN mvn clean package -DskipTests -pl microservices/product-microservice -am

#2 etapa
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl bash
WORKDIR /app

COPY --from=build /app/microservices/product-microservice/target/*.jar app.jar

EXPOSE 8091

ENTRYPOINT ["java", "-jar", "app.jar"]