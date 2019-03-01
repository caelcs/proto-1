#!/usr/bin/env bash

set -ex

create_context(){
    kubectl create --filename ./k8s/namespace.yml
}

create_zoo_pod(){
	kubectl create -n proto1 --filename ./k8s/zookeeper-deployment.yml
}

create_zoo_service(){
	kubectl create -n proto1 --filename ./k8s/zookeeper-service.yml
}

create_kafka_pod(){
	kubectl create -n proto1 -f ./k8s/kafka-deployment.yml
}

create_kafka_service(){
	kubectl create -n proto1 -f ./k8s/kafka-service.yml
}

delete_kafka_service(){
	kubectl delete -n proto1 -f ./k8s/kafka-service.yml
}

delete_kafka_pod(){
	kubectl delete -n proto1 -f ./k8s/kafka-deployment.yml
}

delete_zoo_service(){
	kubectl delete -n proto1 -f ./k8s/zookeeper-service.yml
}

delete_zoo_pod(){
	kubectl delete -n proto1 -f ./k8s/zookeeper-deployment.yml
}

$@