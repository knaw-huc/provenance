FROM openjdk:17-alpine AS build

COPY . /app
WORKDIR /app

RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../provenance-1.0.jar)

FROM openjdk:17-alpine

COPY --from=build /app/target/dependency/ /app
EXPOSE 8080

ENTRYPOINT ["java", "-cp", "/app", "org.knaw.huc.provenance.Application"]
