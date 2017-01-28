#!/usr/bin/env bash

FORMIC_SERVER="http://10.200.1.67:80"
JAVA_OPTS="-DformicServer=$FORMIC_SERVER"
export JAVA_OPTS
DATATYPEID=`./gatling-charts-highcharts-bundle-2.2.1/bin/gatling.sh -s de.tu_berlin.formic.gatling.experiment.LinearInsertPreparationSimulation | grep "Id" | tr -d "Id: "`
echo "Id: $DATATYPEID"
./executeGatlingTest.sh $1 $DATATYPEID
