# Foody - Food Delivery Platform

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/nelson-tanko/foody)
[![Java](https://img.shields.io/badge/java-21-orange)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

'Foody' is a comprehensive backend food delivery platform built with Spring Boot. The application provides a robust API for managing restaurants, menu items, orders, user ratings, and comments.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
    - [Clone the Repository](#clone-the-repository)
    - [Running with Docker](#running-with-docker)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Available Commands](#available-commands)
- [Project Structure](#project-structure)
- [License](#license)

## Features

- **User Management**: Registration, authentication, and role-based access control
- **Restaurant Management**: Create, update, and search for restaurants
- **Menu Management**: Add, update, and remove menu items
- **Order Processing**: Place, track, and manage food orders
- **Review System**: Rate restaurants and add comments
- **Search Functionality**: Find restaurants and menu items based on various criteria
- **Caching**: Optimized performance using Spring Cache
- **Scheduled Tasks**: Automated processes for order management and reporting
- **Monitoring**: With Prometheus and Grafana

## Tech Stack

- **Java 21**: Core programming language
- **Spring Boot**: Main framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database operations
- **PostgreSQL**: Primary database
- **Redis**: Caching and message broker
- **Flyway**: Database migrations
- **Hibernate Validator**: Data validation
- **Mapstruct**: Data mapping
- **Spring Cache**: Caching mechanism
- **JUnit 5**: Testing framework
- **Mockito**: Mocking library for unit tests
- **Testcontainers**: Integration testing with containerized dependencies
- **Docker**: Containerization
- **Maven**: Build tools
- **Prometheus and Grafana**: For monitoring

## Requirements

- Java 21 or higher
- Docker and Docker Compose
- Maven (included as wrapper)
- Make (for running Makefile commands)

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/nelsontanko/foody.git
cd foody
```

### Running with Docker 

To run the entire application stack using Docker:

```bash
make up
make run
```
The application will be available at `http://localhost:8080`

To stop all containers:

```bash
make down
```

## API Documentation

Once the application is running, you can access the API documentation at:

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testing

### Running Tests

```bash
# Run all tests
make test

# Run only integration tests
make test-integration
```

## Monitoring

For Prometheus dashboard, go to http://localhost:9090
For Grafana dashboard, go to http://localhost:3000

## Available Commands

The project includes a Makefile with the following commands:

```bash
# Development
make build              - Build the application
make clean              - Clean build artifacts
make test               - Run tests
make test-integration   - Run integration tests
make run                - Run the application locally

# Docker operations
make docker-logs        - Show logs from the Docker container
make docker-shell       - Get a shell in the Docker container

# Docker Compose operations
make up                 - Start all services with Docker Compose
make down               - Stop all services with Docker Compose

# Database operations
make db-connect         - Connect to PostgreSQL database
```

## Project Structure

```
├───.idea
├───.mvn
│   └───wrapper
├───src
│   ├───main
│   │   ├───java
│   │   │   └───dev
│   │   │       ├───account/            # Account configuration
│   │   │       ├───core
│   │   │       │   ├───common          # Shared classes
│   │   │       │   ├───config          # Application configuration
│   │   │       │   ├───dto             # Data Transfer Objects
│   │   │       │   ├───exception       # Custom exception
│   │   │       │   ├───spec            # Specification class
│   │   │       │   ├───utils           # Utility classes
│   │   │       │   └───validation      # Validation classes
│   │   │       ├───security/            # Security configuration
│   │   │       └───services/           # Business logic
│   │   │           
│   │   └───resources
│   │       ├───config/                  # Application configuration
│   │       ├───db
│   │       │   └───migration           # Flyway migration scripts
│   │       └───swagger
│   └── test/                           # Test classes
├── docker-compose.yml                  # Docker Compose configuration
├── Dockerfile                          # Docker configuration
├── Makefile                            # Project commands
├── mvnw                                # Maven wrapper
├── pom.xml                             # Maven configuration
└── README.md                           # Project documentation
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
