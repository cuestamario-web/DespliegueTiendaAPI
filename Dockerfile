# ===== Etapa 1: compilar el proyecto con Maven =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package

# ===== Etapa 2: imagen liviana que solo ejecuta el jar =====
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/tienda-api-1.0-jar-with-dependencies.jar app.jar
RUN mkdir -p data && touch data/usuarios.txt data/productos.txt
EXPOSE 4567
CMD ["java", "-jar", "app.jar"]
