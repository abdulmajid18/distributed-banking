# Stop all services
docker compose down

# Remove existing volumes (clean slate)
docker volume rm $(docker volume ls -q | grep banking)

# Start Zookeeper only
docker compose up -d zookeeper

# Initialize metadata
docker run --rm --network banking_banking-net \
  -e BK_zkServers=zookeeper:2181 \
  apache/bookkeeper:4.17.0 \
  /opt/bookkeeper/bin/bookkeeper shell metaformat -nonInteractive

# Start everything
docker compose up -d