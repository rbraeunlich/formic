#!/usr/bin/env bash
GATLING_HOME=gatling-charts-highcharts-bundle-2.2.1
JAVA_OPTS="$1 $2 $3 $4"
GATLING_RUNNER=$5
SIMULATION_NAME=$6

. ./$GATLING_RUNNER -nr -s $SIMULATION_NAME > gatling-run.log 2>&1 &