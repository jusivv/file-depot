# build stage
FROM maven:3.8.4-openjdk-11 AS build
WORKDIR /src/file-depot
COPY ./ ./
RUN mvn clean package -U -pl fd-webapp -am -Dmaven.test.skip=true

# package stage
FROM openjdk:11.0-jre
WORKDIR /app
COPY --from=build /src/file-depot/fd-webapp/target/lib ./lib
COPY --from=build /src/file-depot/fd-webapp/target/config ./config
COPY --from=build /src/file-depot/fd-webapp/target/file-depot-webapp.jar ./
CMD ["java", "-jar", "file-depot-webapp.jar"]
EXPOSE 8090