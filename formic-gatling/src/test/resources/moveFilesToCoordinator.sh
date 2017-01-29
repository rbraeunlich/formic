#!/usr/bin/env bash
USER_NAME='ubuntu'
COORDINATOR="10.200.1.35"
USER_HOME=/home/$USER_NAME
GATLING_HOME=$USER_HOME/gatling-charts-highcharts-bundle-2.2.1
GATLING_LIB_DIR=$GATLING_HOME/lib
ASSEMBLY=/Users/ronny/Entwicklung/Masterthesis/scala-workspace/formic/formic-gatling/target/scala-2.11/formic-gatling-assembly-1.0.0.jar
TEST_ASSEMBLY=/Users/ronny/Entwicklung/Masterthesis/scala-workspace/formic/formic-gatling/target/scala-2.11/formic-gatling-test-1.0.0.jar
KEY=/Users/ronny/cloud.key
EXECUTION_SCRIPT=/Users/ronny/Entwicklung/Masterthesis/scala-workspace/formic/formic-gatling/src/test/resources/executeGatlingTest.sh
PREPARATION_SCRIPT=/Users/ronny/Entwicklung/Masterthesis/scala-workspace/formic/formic-gatling/src/test/resources/executeGatlingTestWithPreparation.sh

scp -i $KEY $ASSEMBLY $USER_NAME@$COORDINATOR:$GATLING_LIB_DIR
scp -i $KEY $TEST_ASSEMBLY $USER_NAME@$COORDINATOR:$GATLING_LIB_DIR
scp -i $KEY $KEY $USER_NAME@$COORDINATOR:$USER_HOME
scp -i $KEY $EXECUTION_SCRIPT $USER_NAME@$COORDINATOR:$USER_HOME
scp -i $KEY $PREPARATION_SCRIPT $USER_NAME@$COORDINATOR:$USER_HOME