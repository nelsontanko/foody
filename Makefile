APP_NAME = foody-app
VERSION ?= latest

ifeq ($(OS),Windows_NT)
    MVN = mvnw
else
    MVN = ./mvnw
endif

DOCKER_COMPOSE_DEFAULT = docker-compose -f compose.yml
DOCKER_COMPOSE_ALL = docker-compose -f compose.yml -f docker/monitoring.yml

.PHONY: help build clean test run test-integration \
        docker-logs docker-shell up down db-connect \
        up-all down-all

help:
	@echo "Available commands:"
	@echo "  make build              - Build the application"
	@echo "  make clean              - Clean build artifacts"
	@echo "  make test               - Run tests"
	@echo "  make test-integration   - Run integration tests"
	@echo "  make run                - Run the application locally"
	@echo "  make run --all          - Run with Docker Compose (all services including monitoring)"
	@echo "  make docker-logs        - Show logs from the Docker container"
	@echo "  make docker-shell       - Get a shell in the Docker container"
	@echo "  make docker-push        - Push the Docker image to registry"
	@echo "  make up                 - Start main services with Docker Compose"
	@echo "  make up --all           - Start all services including monitoring"
	@echo "  make up-all             - Start all services including monitoring (alternative)"
	@echo "  make down               - Stop services (automatically detects which were started)"
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

ifeq ($(filter --all,$(MAKECMDGOALS)),--all)
    COMPOSE_CMD = $(DOCKER_COMPOSE_ALL)
    SERVICE_TYPE = all services (including monitoring)
else
    COMPOSE_CMD = $(DOCKER_COMPOSE_DEFAULT)
    SERVICE_TYPE = main services
endif

run:
ifeq ($(filter --all,$(MAKECMDGOALS)),--all)
	@echo "Starting $(SERVICE_TYPE) with Docker Compose..."
	$(COMPOSE_CMD) up -d --build
	@echo "$(SERVICE_TYPE) started"
else
	@echo "Starting application locally with Maven..."
	$(MVN) spring-boot:run
	@echo "Application started"
endif

# Handle the --all flag as a no-op target
--all:
	@:

docker-stop:
	docker stop $(APP_NAME) || true
	docker rm $(APP_NAME) || true
	@echo "Docker container stopped and removed"

docker-logs:
	docker logs -f $(APP_NAME)

docker-shell:
	docker exec -it $(APP_NAME) /bin/sh

up:
ifeq ($(filter --all,$(MAKECMDGOALS)),--all)
	@echo "Starting all services (including monitoring)..."
	$(DOCKER_COMPOSE_ALL) up -d --build
	@echo "All services started"
else
	@echo "Starting main services..."
	$(DOCKER_COMPOSE_DEFAULT) up -d --build
	@echo "Main services started"
endif

up-all:
	@echo "Starting all services (including monitoring)..."
	$(DOCKER_COMPOSE_ALL) up -d --build
	@echo "All services started"

down:
	@echo "Stopping all services..."
	$(DOCKER_COMPOSE_ALL) down
	@echo "All services stopped"

db-connect:
	docker exec -it foody-db psql -U postgres -d foody

redis:
	docker exec -it foody-redis bash

version:
	@echo "Building version: $(VERSION)"