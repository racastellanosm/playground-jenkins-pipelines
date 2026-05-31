SHELL				:= /bin/bash
UID 				:= $(shell id -u)
GID 				:= $(shell id -g)
DOCKER_COMPOSE		:= docker compose -f docker-compose.yml

# Colors using ANSI escape sequences
GREEN  := \033[1;32m
RED    := \033[1;31m
YELLOW := \033[1;33m
RESET  := \033[0m

.PHONY: help build start stop logs logs-agents status .cleanup .setup

help: ## Show this help message
	@printf "$(GREEN)-------------- USAGE --------------------------------------$(RESET)\n"
	@printf "$$ make $(GREEN)target$(RESET) [options]\n"
	@printf "$(GREEN)-------------- Available Targets ---------------------------$(RESET)\n"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "$(GREEN)%-20s$(RESET) %s\n", $$1, $$2}'

build: .setup ## Build the custom Jenkins controller image and pull agents
	@printf "$(YELLOW)Building Jenkins playground images...$(RESET)\n"
	@$(DOCKER_COMPOSE) down --remove-orphans -v --rmi local || $(MAKE) .cleanup
	@$(DOCKER_COMPOSE) build --pull || $(MAKE) .cleanup
	@printf "$(GREEN)Build complete!$(RESET)\n"

start: .setup ## Start the Jenkins playground stack in the background
	@printf "$(YELLOW)Starting Jenkins playground stack...$(RESET)\n"
	@$(DOCKER_COMPOSE) up -d || $(MAKE) .cleanup
	@printf "$(GREEN)Stack successfully started!$(RESET)\n"
	@printf "$(YELLOW)Jenkins UI is starting up at:$(RESET) http://localhost:8080 (admin/admin)\n"

stop: ## Stop the stack and purge all container volumes and orphans
	@printf "$(YELLOW)Stopping Jenkins playground stack and purging state...$(RESET)\n"
	@$(DOCKER_COMPOSE) down --remove-orphans -v
	@printf "$(GREEN)Stack stopped and all state purged!$(RESET)\n"

logs: ## Show and follow the Jenkins controller logs
	@$(DOCKER_COMPOSE) logs jenkins-controller --follow

logs-agents: ## Show and follow the Jenkins agents logs
	@$(DOCKER_COMPOSE) logs jenkins-agent-amd64 jenkins-agent-arm64 --follow

status: ## Show the status of the playground containers
	@$(DOCKER_COMPOSE) ps

## Helper Functions
.cleanup:
	@$(DOCKER_COMPOSE) down --remove-orphans -v
	@printf "$(RED)Stack stopped with error!$(RESET)\n"
	@exit 1

.setup:
	@if [ ! -f secrets/jenkins-agent-key ]; then \
		printf "$(YELLOW)SSH keys not found. Generating new keypair...$(RESET)\n"; \
		mkdir -p secrets; \
		ssh-keygen -t ed25519 -f secrets/jenkins-agent-key -N ""; \
		printf "JENKINS_AGENT_SSH_PUBKEY=\"$$(cat secrets/jenkins-agent-key.pub)\"\n" > .env; \
		printf "$(GREEN)Keys generated and saved to secrets/$(RESET)\n"; \
	fi
