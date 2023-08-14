#!/bin/bash

TARGET="localhost"
APP="localhost"
CONTENT="localhost/content"
CAMUNDA="localhost/bpm"

check_service(){
  TARGET=$1
  brbool="false"
  for i in $(seq 1 50);
  do
    HTTP_STAT=$(curl -o /dev/null -s -w "%{http_code}\n" $TARGET)
    if [ "$HTTP_STAT" == "200" ]; then
      echo "$TARGET alive and web site is up"
      brbool="true"
      break;
    else
      echo "$TARGET offline or web server problem $i check with status code $HTTP_STAT"
      sleep 5;
    fi
  done
  if [ "$brbool" == "false" ]; then
    echo "============================"
    docker-compose logs
    docker ps
    echo "exit 1"
    exit 1
  fi
}

check_service $APP &&
check_service "$CONTENT/actuator/health" &&
check_service "$CAMUNDA/actuator/health" &&
wait