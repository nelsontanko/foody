#FROM eclipse-temurin:21-jdk-alpine AS builder
#
#WORKDIR /app
#
#COPY ./mvnw ./
#COPY ./.mvn ./.mvn/
#RUN chmod +x ./mvnw
#
#COPY ./pom.xml ./
#
#RUN ./mvnw dependency:go-offline -B
#
#COPY ./src ./src
#
#RUN ./mvnw clean package -DskipTests -q
#
## Runtime stage - use distroless for better security
##FROM gcr.io/distroless/java21-debian12:nonroot
#
#FROM eclipse-temurin:21-jre-alpine
#RUN apk add --no-cache dumb-init && \
#     addgroup --system --gid 1001 appgroup && \
#     adduser --system --uid 1001 --ingroup appgroup appuser
#
#WORKDIR /app
#
#COPY --from=builder /app/target/*.jar /app/app.jar
#
#ENV JAVA_OPTS="-XX:+UseContainerSupport \
#               -XX:MaxRAMPercentage=75.0 \
#               -XX:+UseG1GC \
#               -XX:+UseStringDeduplication \
#               -Djava.security.egd=file:/dev/./urandom \
#               -Dspring.profiles.active=docker"
#
#HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
#    CMD curl -f http://localhost:8080/actuator/health || exit 1
#
#EXPOSE 8080
#
#ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY ./pom.xml ./
COPY ./mvnw ./
COPY ./.mvn ./.mvn/

RUN ./mvnw dependency:go-offline -B

COPY ./src ./src

RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache dumb-init && \
     addgroup --system --gid 1001 appgroup && \
     adduser --system --uid 1001 --ingroup appgroup appuser

WORKDIR /app

COPY --from=builder /app/target/*.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/management/health || exit 1

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
