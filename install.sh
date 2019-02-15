#!/bin/bash

echo "Deleting volumes for zookeeper"
rm -rf docker/zoo1

echo "stopping all containers if there are running"
docker-compose -f docker/infrastructure.yml -f docker/apps.yml down

echo "build apps"
./gradlew :producer:clean :producer:build
./gradlew :consumer:clean :consumer:build

echo "build images for the apps"
docker build -t adolfoecs/consumer:latest ./consumer
docker build -t adolfoecs/producer:latest ./producer

echo "running kafka and zookeeper"
docker-compose -f docker/infrastructure.yml up -d

sleep 5

echo "creating topic"
docker exec docker_kafka1_1 kafka-topics --create --zookeeper zoo1:2181 --replication-factor 1 --partitions 3 --topic topicTest3
docker exec docker_kafka1_1 kafka-topics --describe --zookeeper zoo1:2181 --topic topicTest3

echo "running consumer and producer"
docker-compose -f docker/infrastructure.yml -f docker/apps.yml up -d