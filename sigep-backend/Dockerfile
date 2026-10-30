# etapa de build
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src/ ./src/

RUN ./mvnw clean package -DskipTests -B

# etapa de execução
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia o JAR produzido na etapa de build
COPY --from=build /app/target/*.jar app.jar

# Documenta a porta utilizada pela aplicação
EXPOSE 8080

# Inicializa o Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]