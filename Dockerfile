FROM maven:3.9-eclipse-temurin-21 AS build

COPY . /app
WORKDIR /app

RUN mvn install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../provenance-1.0.jar)

FROM eclipse-temurin:21-jre

COPY --from=build /app/target/dependency/ /app
EXPOSE 8080

ENTRYPOINT ["java", "-cp", "/app", "org.knaw.huc.provenance.Application"]
