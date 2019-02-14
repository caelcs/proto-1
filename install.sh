#!/bin/bash

docker kill $(docker ps -a | grep consumer  | awk '{print $1}')
docker kill $(docker ps -a | grep producer  | awk '{print $1}')
docker kill $(docker ps -a | grep kafka     | awk '{print $1}')
docker kill $(docker ps -a | grep zookeeper | awk '{print $1}')

docker volume ls -qf dangling=true | xargs docker volume rm

docker rm $(docker ps -a -q)

docker rmi $(docker images -a | grep consumer | awk '{print $1}')
docker rmi $(docker images -a | grep producer | awk '{print $1}')
docker rmi $(docker images -a | grep kafka    | awk '{print $1}')
docker rmi $(docker images -a | grep zookeeper    | awk '{print $1}')

./gradlew :producer:clean :producer:build
./gradlew :consumer:clean :consumer:build

docker build -t adolfoecs/consumer:latest ./consumer
docker build -t adolfoecs/producer:latest ./producer

docker-compose -f docker/platform.yml up -d