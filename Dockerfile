FROM eclipse-temurin:17-jdk-jammy AS java-test

WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src

CMD ["./mvnw", "verify"]
