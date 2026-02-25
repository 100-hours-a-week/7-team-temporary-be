# stage 1
FROM eclipse-temurin:25-jdk AS deps
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

RUN --mount=type=cache,target=/root/.gradle \
    chmod +x gradlew && ./gradlew dependencies --no-daemon

# stage 2
FROM deps AS build
COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon \
    && java -Djarmode=layertools -jar build/libs/*.jar extract --destination extracted

# stage 3
FROM eclipse-temurin:25-jre-jammy AS runtime

RUN addgroup --system spring && adduser --system --ingroup spring spring
WORKDIR /app

COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./

RUN chown -R spring:spring /app
USER spring
EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:MaxRAMPercentage=75.0", \
    "org.springframework.boot.loader.launch.JarLauncher"]