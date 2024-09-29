# FROM maven:3.8.2-jdk-8 # for Java 8
#FROM maven:3.8.5-openjdk-17
#
#WORKDIR /bezkoder-app
#COPY . .
#RUN mvn clean install
#
#CMD mvn spring-boot:run

# Use a specific base image with Maven and Java runtime
FROM maven:3.8.4-openjdk-17-slim AS build

# Set the working directory in the container
WORKDIR /app

# Copy the entire project into the Docker image
COPY . .

# Build the project using Maven
RUN mvn clean package -DskipTests

# Use a specific base image with Java runtime for the final image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the build stage into the final image
COPY --from=build /app/target/*.jar escalayt-application.jar

# Expose the port the application runs on
EXPOSE 8080

# Set the entry point to run the application
ENTRYPOINT ["java", "-jar", "escalayt-application.jar"]
