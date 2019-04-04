#!/bin/bash

MACHINE=`docker-machine ls | grep proto-1 | awk '{print $1}'`
echo 'MACHINE' $MACHINE

eval "$(docker-machine env proto-1)"
export DOCKER_MACHINE_IP=`docker-machine ip proto-1`
export CONSUMER_TOPIC="CONSUMER_TOPIC"
export ACK_CONSUMER_TOPIC="ACK_CONSUMER_TOPIC"
export ACK_CONSUMER_CLIENT_ID="clientId"

echo "running consumer and producer"
docker-compose -f docker/infrastructure.yml -f docker/consumer.yml up -d

echo "Installation done"