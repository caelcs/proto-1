#!/bin/bash

MACHINE=`docker-machine ls | grep proto-1 | awk '{print $1}'`
echo 'MACHINE' $MACHINE

if [ -z "$MACHINE" ]
then
    echo 'creating proto-1'
    docker-machine create --driver virtualbox --virtualbox-memory "10024" --virtualbox-disk-size "400000" proto-1
    docker-machine start proto-1
fi

eval "$(docker-machine env proto-1)"
export DOCKER_MACHINE_IP=`docker-machine ip proto-1`
export CONSUMER_TOPIC="CONSUMER_TOPIC"
export ACK_CONSUMER_TOPIC="ACK_CONSUMER_TOPIC"
export ACK_CONSUMER_CLIENT_ID="clientId"


echo "Deleting volumes for zookeeper"
rm -rf docker/zoo1

echo "stopping all containers if there are running"
docker-compose -f docker/infrastructure.yml -f docker/producer.yml -f docker/consumer.yml -f docker/monitor.yml down --remove-orphans

echo "build apps"

if [ -z "$1" ]
then
    ./gradlew :producer:clean :producer:build
    ./gradlew :consumer:clean :consumer:build

    echo "build images for the apps"
    docker build -t adolfoecs/consumer:latest ./consumer
    docker build -t adolfoecs/producer:latest ./producer
fi

echo "running kafka and zookeeper"
docker-compose -f docker/infrastructure.yml up -d

sleep 10

echo "creating topic"
docker exec docker_kafka1_1 kafka-topics --create --zookeeper zoo1:2181 --replication-factor 1 --partitions 3 --topic $CONSUMER_TOPIC
docker exec docker_kafka1_1 kafka-topics --create --zookeeper zoo1:2181 --replication-factor 1 --partitions 3 --topic $ACK_CONSUMER_TOPIC
docker exec docker_kafka1_1 kafka-topics --describe --zookeeper zoo1:2181 --topic $CONSUMER_TOPIC
docker exec docker_kafka1_1 kafka-topics --describe --zookeeper zoo1:2181 --topic $ACK_CONSUMER_TOPIC

echo "running consumer and producer"
docker-compose -f docker/infrastructure.yml -f docker/producer.yml -f docker/consumer.yml -f docker/monitor.yml up -d --scale consumer1=3 --no-recreate

sleep 5

echo "Installation done"
echo "grafana http://"$DOCKER_MACHINE_IP":3000"
echo "prom http://"$DOCKER_MACHINE_IP":9090"
echo "producer http://"$DOCKER_MACHINE_IP":8080"