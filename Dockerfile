FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY ./pom.xml ./
COPY ./mvnw ./
COPY ./.mvn ./.mvn/

RUN ./mvnw dependency:go-offline -B

COPY ./src ./src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup --system appgroup && \
    adduser --system --ingroup appgroup appuser

WORKDIR /app

COPY --from=builder /app/target/*.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

USER appuser

EXPOSE 8080

ENTRYPOINT exec java -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS -jar /app/app.jar
