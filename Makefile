# Flink Java Flights Demo Makefile

# Colors
YELLOW := \033[1;33m
GREEN := \033[1;32m
RED := \033[1;31m
BLUE := \033[1;34m
PURPLE := \033[1;35m
CYAN := \033[1;36m
NC := \033[0m # No Color

.PHONY: help build clean run test docker-up docker-down kafka-topics kafka-list-topics config-local config-cloud schema-test config-test gh-list gh-view gh-logs gh-rerun gh-debug

help:
	@echo "${BLUE}🚀 Flink Java Flights Demo - Available Commands${NC}"
	@echo "${YELLOW}make build${NC}            - 🔨 Build the project"
	@echo "${YELLOW}make clean${NC}            - 🧹 Clean build artifacts"
	@echo "${YELLOW}make run${NC}              - 🏃 Run the Flink application"
	@echo "${YELLOW}make test${NC}             - 🧪 Run tests"
	@echo "${YELLOW}make docker-up${NC}        - 🐳 Start Docker containers"
	@echo "${YELLOW}make docker-down${NC}      - 🛑 Stop Docker containers"
	@echo "${YELLOW}make kafka-topics${NC}     - 📋 Create Kafka topics"
	@echo "${YELLOW}make kafka-list-topics${NC} - 📝 List Kafka topics"
	@echo "${YELLOW}make config-local${NC}     - 🔑 Show local configuration"
	@echo "${YELLOW}make config-cloud${NC}     - 🌏 Show cloud configuration"
	@echo "${YELLOW}make schema-test${NC}      - 📊 Run schema tests"
	@echo "${YELLOW}make config-test${NC}      - 📊 Run config tests"
	@echo "${CYAN}make gh-list${NC}          - 📋 List recent GitHub Actions workflow runs"
	@echo "${CYAN}make gh-view${NC}           - 🔎 View details of a specific workflow run"
	@echo "${CYAN}make gh-logs${NC}           - 📝 View logs of a specific workflow run"
	@echo "${CYAN}make gh-rerun${NC}          - 🔁 Re-run a failed workflow"
	@echo "${CYAN}make gh-debug${NC}          - 🔬 Debug a workflow in real-time"

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
	./scripts/start-env.sh

docker-down:
	@echo "${RED}🛑 Stopping Docker containers...${NC}"
	./scripts/stop-env.sh

kafka-topics:
	@echo "${YELLOW}📋 Creating Kafka topics...${NC}"
	docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --create --if-not-exists --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --topic flights
	docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --create --if-not-exists --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --topic flight_alerts
	@echo "${GREEN}✅ Kafka topics created!${NC}"

kafka-list-topics:
	@echo "${BLUE}📝 Listing Kafka topics...${NC}"
	docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092

config-local:
	@echo "${CYAN}🔑 Showing local configuration...${NC}"
	./gradlew run --args='com.example.config.ConfigurationTool local'

config-cloud:
	@echo "${PURPLE}🌏 Showing cloud configuration...${NC}"
	./gradlew run --args='com.example.config.ConfigurationTool cloud'

schema-test:
	@echo "${GREEN}📊 Running schema tests...${NC}"
	./gradlew test --tests FlightSchemaTest

config-test:
	@echo "${GREEN}📊 Running configuration tests...${NC}"
	./gradlew test --tests ConfigurationManagerTest

# GitHub Actions workflow commands
gh-list:
	@echo "${CYAN}📋 Listing recent GitHub Actions workflow runs...${NC}"
	gh run list --limit 10

gh-view:
	@echo "${PURPLE}🔎 Viewing details for workflow run $(id)...${NC}"
	@if [ -z "$(id)" ]; then \
		echo "${RED}❌ Error: Run ID required. Usage: make gh-view id=<run-id>${NC}"; \
		exit 1; \
	fi
	gh run view $(id)

gh-logs:
	@echo "${YELLOW}📝 Fetching logs for workflow run $(id)...${NC}"
	@if [ -z "$(id)" ]; then \
		echo "${RED}❌ Error: Run ID required. Usage: make gh-logs id=<run-id>${NC}"; \
		exit 1; \
	fi
	gh run view $(id) --log

gh-rerun:
	@echo "${GREEN}🔁 Re-running workflow $(id)...${NC}"
	@if [ -z "$(id)" ]; then \
		echo "${RED}❌ Error: Run ID required. Usage: make gh-rerun id=<run-id>${NC}"; \
		exit 1; \
	fi
	gh run rerun $(id)

gh-debug:
	@echo "${BLUE}🔬 Starting debug workflow watch...${NC}"
	gh run watch
