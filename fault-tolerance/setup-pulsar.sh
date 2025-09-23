#!/bin/bash
# Stop all services
docker compose down

# Remove existing volumes (clean slate)
docker volume rm $(docker volume ls -q | grep banking) 2>/dev/null || true

# Create data directories with proper permissions
echo "Creating data directories..."
mkdir -p ./data/zookeeper ./data/bookkeeper
chmod -R 777 ./data  # Ensure write permissions

# Start the Pulsar cluster
echo "Starting Pulsar cluster with Docker Compose..."
docker compose up -d