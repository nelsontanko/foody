APP_NAME = foody-app
VERSION ?= latest

ifeq ($(OS),Windows_NT)
    MVN = mvnw
else
    MVN = ./mvnw
endif

DOCKER_COMPOSE = docker-compose -f compose.yaml

.PHONY: help build clean test run test-integration \
        docker-logs docker-shell docker-compose-up docker-compose-down db-connect

help:
	@echo "Available commands:"
	@echo "  make build              - Build the application"
	@echo "  make clean              - Clean build artifacts"
	@echo "  make test               - Run tests"
	@echo "  make test-integration   - Run integration tests"
	@echo "  make run                - Run the application locally"
	@echo "  make docker-logs        - Show logs from the Docker container"
	@echo "  make docker-shell       - Get a shell in the Docker container"
	@echo "  make docker-push        - Push the Docker image to registry"
	@echo "  make docker-compose-up  - Start all services with Docker Compose"
	@echo "  make docker-compose-down - Stop all services with Docker Compose"
	@echo "  make db-connect         - Connect to PostgreSQL database"
	@echo "  make redis              - Connect to Redis"
	@echo "  make version            - Set version number for the build (make version VERSION=1.2.3)"

build:
	$(MVN) clean package -DskipTests
	@echo "Build completed"

clean:
	$(MVN) clean
	@echo "Cleaned build artifacts"

test:
	$(MVN) test
	@echo "Tests completed"

test-integration:
	$(MVN) verify -Dskip.unit.tests=true
	@echo "Integration tests completed"

run:
	$(MVN) spring-boot:run
	@echo "Application started"

docker-stop:
	docker stop $(APP_NAME) || true
	docker rm $(APP_NAME) || true
	@echo "Docker container stopped and removed"

docker-logs:
	docker logs -f $(APP_NAME)

docker-shell:
	docker exec -it $(APP_NAME) /bin/sh

docker-compose-up:
	$(DOCKER_COMPOSE) up -d --build
	@echo "All services started"

docker-compose-down:
	$(DOCKER_COMPOSE) down
	@echo "All services stopped"

db-connect:
	docker exec -it foody-db psql -U postgres -d foody

redis:
	docker exec -it foody-redis bash

version:
	@echo "Building version: $(VERSION)"