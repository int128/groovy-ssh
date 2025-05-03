#!/bin/bash -xe

exec docker-compose -f $( dirname -- "$0"; )/../.circleci/docker-compose.yml up
