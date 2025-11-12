#!/usr/bin/env bash

TAG=$RANDOM
NAME=marcinbablok/redbutton-websocket-poc

#sbt assembly
docker login
docker buildx build --platform linux/amd64,linux/arm64 -t $NAME:$TAG -t $NAME:latest --push .
