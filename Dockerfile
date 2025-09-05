FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY src/main/resources ./src/main/resources
RUN mvn clean package -DskipTests

FROM amazoncorretto:21
WORKDIR /app
COPY --from=build /app/target/filmorate-0.0.1-SNAPSHOT.jar ./filmorate.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "filmorate.jar"]