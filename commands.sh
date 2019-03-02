#!/usr/bin/env bash

set -ex

create_context(){
    kubectl create --filename ./k8s/namespace.yml
}

install_zoo(){
    echo "installing zookeeper"
	kubectl create -n proto1 --filename ./k8s/zookeeper-deployment.yml
	kubectl create -n proto1 --filename ./k8s/zookeeper-service.yml
}

install_kafka(){
    echo "installing kafka"
	kubectl create -n proto1 -f ./k8s/kafka-deployment.yml
	kubectl create -n proto1 -f ./k8s/kafka-service.yml
}

install_node_exporter(){
    echo "installing node exporter"
	kubectl create -n proto1 -f ./k8s/node-exporter-deployment.yml
	kubectl create -n proto1 -f ./k8s/node-exporter-service.yml
}

install_prom(){
    echo "installing prometheus"
	kubectl create configmap prom-config-map --from-file=provisioning/monitor/prom -n proto1
	kubectl create -n proto1 -f ./k8s/prom-deployment.yml
	kubectl create -n proto1 -f ./k8s/prom-service.yml
}

install_grafana(){
    echo "installing grafana"
	kubectl create configmap grafana-config-map --from-file=provisioning/monitor/grafana -n proto1
	kubectl create -n proto1 -f ./k8s/grafana-deployment.yml
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
    install_zoo
    install_kafka
    install_node_exporter
    install_prom
    install_grafana
}

$@