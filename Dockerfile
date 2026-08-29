# =========================================================
# ESTAGIO 1 - Build
# Usa JDK completo: precisa compilar.
# =========================================================
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copiados primeiro e SOZINHOS, de propósito.
# O Docker guarda cada camada em cache e só refaz da primeira
# mudança em diante. Como o pom.xml muda raramente e o código
# muda a cada commit, isolar o download de dependências aqui
# faz com que ele seja reaproveitado do cache na maioria dos builds.
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Só agora o código-fonte. Alterações aqui invalidam apenas
# esta camada e as seguintes, não o download de dependências.
COPY src src

RUN ./mvnw clean package -DskipTests -B

# =========================================================
# ESTAGIO 2 - Runtime
# Usa apenas o JRE. Compilador, Maven, código-fonte e cache
# de dependências ficam para trás, reduzindo drasticamente o
# tamanho da imagem final e a superfície de vulnerabilidades.
# =========================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Usuário sem privilégios administrativos. Por padrão o processo
# dentro do contêiner roda como root; se a aplicação for explorada,
# o atacante herda esse poder. Um usuário comum limita o alcance.
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring

ENV TZ=America/Sao_Paulo

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]