# Flink Java Flights Demo Makefile

# Colors
YELLOW := \033[1;33m
GREEN := \033[1;32m
RED := \033[1;31m
BLUE := \033[1;34m
PURPLE := \033[1;35m
CYAN := \033[1;36m
NC := \033[0m # No Color

.PHONY: help build clean run test docker-up docker-down

help:
	@echo "${BLUE}🚀 Flink Java Flights Demo - Available Commands${NC}"
	@echo "${YELLOW}make build${NC}       - 🔨 Build the project"
	@echo "${YELLOW}make clean${NC}       - 🧹 Clean build artifacts"
	@echo "${YELLOW}make run${NC}         - 🏃 Run the Flink application"
	@echo "${YELLOW}make test${NC}        - 🧪 Run tests"
	@echo "${YELLOW}make docker-up${NC}   - 🐳 Start Docker containers"
	@echo "${YELLOW}make docker-down${NC} - 🛑 Stop Docker containers"

build:
	@echo "${GREEN}🔨 Building project...${NC}"
	./gradlew build

clean:
	@echo "${BLUE}🧹 Cleaning project...${NC}"
	./gradlew clean

run:
	@echo "${GREEN}🏃 Running Flink application...${NC}"
	./gradlew run

test:
	@echo "${PURPLE}🧪 Running tests...${NC}"
	./gradlew test

docker-up:
	@echo "${CYAN}🐳 Starting Docker containers...${NC}"
	./gradlew composeUp

docker-down:
	@echo "${RED}🛑 Stopping Docker containers...${NC}"
	./gradlew composeDown
