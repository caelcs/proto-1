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

create_node_exporter_pod(){
	kubectl create -n proto1 -f ./k8s/node-exporter-deployment.yml
}

create_node_exporter_service(){
	kubectl create -n proto1 -f ./k8s/node-exporter-service.yml
}

create_prom_config_map(){
	kubectl create -n proto1 -f ./k8s/prom-configmap.yml
}

create_prom_pod(){
	kubectl create -n proto1 -f ./k8s/prom-deployment.yml
}

create_prom_service(){
	kubectl create -n proto1 -f ./k8s/prom-service.yml
}

create_grafana_config_map(){
	kubectl create configmap grafana-config-map --from-file=docker/monitor/grafana -n proto1
}

create_grafana_pod(){
	kubectl create -n proto1 -f ./k8s/grafana-deployment.yml
}

create_grafana_service(){
	kubectl create -n proto1 -f ./k8s/grafana-service.yml
}

deleteAll(){
    kubectl delete services --all -n proto1
    kubectl delete deployments --all -n proto1
    kubectl delete configmaps --all -n proto1
    kubectl delete namespace proto1
}

installAll(){
    create_context
    create_zoo_pod
    create_zoo_service
    create_kafka_pod
    create_kafka_service
    create_node_exporter_pod
    create_node_exporter_service
    create_prom_config_map
    create_prom_pod
    create_prom_service
    create_grafana_config_map
    create_grafana_pod
    create_grafana_service
}

$@