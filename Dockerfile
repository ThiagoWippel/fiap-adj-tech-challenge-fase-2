# Build
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# pom e wrapper antes do código, para a camada de dependências ficar em cache
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src

RUN ./mvnw clean package -DskipTests -B

# Runtime: só o JRE
FROM eclipse-temurin:21-jre-alpine AS runtime

# Usuário sem privilégio de root
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring

ENV TZ=America/Sao_Paulo

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
