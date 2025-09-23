#!/bin/bash

# Colors for output
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Destroying Pulsar cluster...${NC}"

# Stop all services and remove containers
docker compose down

# Remove volumes
echo -e "${YELLOW}Removing volumes...${NC}"
docker volume rm $(docker volume ls -q | grep banking) 2>/dev/null || echo "No volumes to remove"

# Remove network
echo -e "${YELLOW}Removing network...${NC}"
docker network rm banking_banking-net 2>/dev/null || echo "Network already removed"

echo -e "${YELLOW}Pulsar cluster destroyed successfully!${NC}"