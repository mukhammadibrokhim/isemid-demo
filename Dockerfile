FROM maven:3-eclipse-temurin-25-alpine AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests && test -f target/app.jar

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S -u 10001 app -G app
COPY --from=build /app/target/app.jar /app/app.jar
USER app
EXPOSE 8081
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]