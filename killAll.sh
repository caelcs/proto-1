#!/bin/bash

eval "$(docker-machine env proto-1)"
export DOCKER_MACHINE_IP=`docker-machine ip proto-1`
export CONSUMER_TOPIC="CONSUMER_TOPIC"
export ACK_CONSUMER_TOPIC="ACK_CONSUMER_TOPIC"
export ACK_CONSUMER_CLIENT_ID="clientId"

echo "killing all pods"
docker-compose -f docker/infrastructure.yml -f docker/producer.yml -f docker/consumer.yml -f docker/monitor.yml down --remove-orphans

echo "Finished"