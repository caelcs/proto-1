#!/bin/bash

docker-compose -f docker/infrastructure.yml -f docker/apps.yml -f docker/monitor.yml down