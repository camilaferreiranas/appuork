
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B


COPY src ./src
RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:25-jre
WORKDIR /app


RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]