#!/bin/bash

MACHINE=`docker-machine ls | grep proto-1 | awk '{print $1}'`
echo 'MACHINE' $MACHINE

eval "$(docker-machine env proto-1)"
export DOCKER_MACHINE_IP=`docker-machine ip proto-1`
export CONSUMER_TOPIC="CONSUMER_TOPIC"

echo "creating topic"
docker exec docker_kafka1_1 kafka-topics --alter --zookeeper zoo1:2181 --partitions $1 --topic $CONSUMER_TOPIC
docker exec docker_kafka1_1 kafka-topics --describe --zookeeper zoo1:2181 --topic $CONSUMER_TOPIC

echo "Finished. You have "$0" partitions now."