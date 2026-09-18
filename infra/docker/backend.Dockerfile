# syntax=docker/dockerfile:1

FROM gradle:8.7-jdk17 AS build
WORKDIR /src
COPY backend/ /src/
RUN gradle --no-daemon installDist

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /src/build/install/backend/ /app/
USER app
EXPOSE 8080
ENTRYPOINT ["/app/bin/backend"]
