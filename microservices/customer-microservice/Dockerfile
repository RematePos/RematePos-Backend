FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache maven

COPY pom.xml pom.xml
COPY microservices/pom.xml microservices/pom.xml
COPY microservices/cart-microservice/pom.xml microservices/cart-microservice/pom.xml
COPY microservices/common-exceptions/pom.xml microservices/common-exceptions/pom.xml
COPY microservices/customer-microservice/pom.xml microservices/customer-microservice/pom.xml
COPY microservices/product-microservice/pom.xml microservices/product-microservice/pom.xml

COPY microservices/common-exceptions/src microservices/common-exceptions/src
COPY microservices/customer-microservice/src microservices/customer-microservice/src

RUN mvn -f microservices/pom.xml clean package -DskipTests -pl customer-microservice -am

#2 etapa
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl bash
WORKDIR /app

COPY --from=build /app/microservices/customer-microservice/target/*.jar app.jar

EXPOSE 8091

ENTRYPOINT ["java", "-jar", "app.jar"]
