#!/bin/bash
##################################################################################################################
#Gatling scale out/cluster run script:
#Before running this script some assumptions are made:
#1) Public keys were exchange inorder to ssh with no password promot (ssh-copy-id on all remotes)
#2) Check  read/write permissions on all folders declared in this script.
#3) Gatling installation (GATLING_HOME variable) is the same on all hosts
#4) Assuming all hosts has the same user name (if not change in script)
##################################################################################################################

if [ -z "$1" ]
    then
        echo "Must provide param for number of users"
        exit 1
fi
if [ -z "$2" ]
    then
        echo "Must provide param for data type instance id"
        exit 1
fi


#Assuming same user name for all hosts
USER_NAME='ubuntu'
FORMIC_SERVER="http://10.200.1.67:80"

#Remote hosts list
if [ $1 -eq 0 ]
    then HOSTS=()
else
    HOSTS=( 192.168.0.27 )
fi

#Simulation options
NUM_EDITORS=$1
DATA_TYPE_ID=$2
JAVA_OPTS="-DformicEditors=$NUM_EDITORS -DformicId=$DATA_TYPE_ID -DformicServer=$FORMIC_SERVER"
export JAVA_OPTS

#Assuming all Gatling installation in same path (with write permissions)
GATLING_HOME=gatling-charts-highcharts-bundle-2.2.1
GATLING_SIMULATIONS_DIR=$GATLING_HOME/user-files/simulations
GATLING_RUNNER=$GATLING_HOME/bin/gatling.sh

#Change to your simulation class name
SIMULATION_NAME='de.tu_berlin.formic.gatling.experiment.LinearInsertSimulation'

#No need to change this
GATLING_REPORT_DIR=$GATLING_HOME/results/
GATHER_REPORTS_DIR=gatling/reports/
GATLING_LIB_DIR=$GATLING_HOME/lib

echo "Starting Gatling cluster run for simulation: $SIMULATION_NAME"

echo "Cleaning previous runs from localhost"
rm -rf $GATHER_REPORTS_DIR
mkdir -p $GATHER_REPORTS_DIR
rm -rf $GATLING_REPORT_DIR

for HOST in "${HOSTS[@]}"
do
  echo "Cleaning previous runs from host: $HOST"
  ssh -n -f -i cloud.key $USER_NAME@$HOST "sh -c 'rm -rf $GATLING_REPORT_DIR'"
done

for HOST in "${HOSTS[@]}"
do
  echo "Copying simulation JARs to host: $HOST"
  scp -i cloud.key $GATLING_LIB_DIR/formic-gatling-assembly-1.0.0.jar $USER_NAME@$HOST:$GATLING_SIMULATIONS_DIR
  scp -i cloud.key $GATLING_LIB_DIR/formic-gatling-test-1.0.0.jar $USER_NAME@$HOST:$GATLING_SIMULATIONS_DIR
done

for HOST in "${HOSTS[@]}"
do
  echo "Running simulation on host: $HOST"
  ssh -n -f -i cloud.key $USER_NAME@$HOST "sh -c \"export JAVA_OPTS=\"$JAVA_OPTS\"; nohup $GATLING_RUNNER -nr -s $SIMULATION_NAME > gatling-run.log 2>&1 &\""
done

echo "Running simulation on localhost"
$GATLING_RUNNER -nr -s $SIMULATION_NAME

echo "Gathering result file from localhost"
ls -t $GATLING_REPORT_DIR | head -n 1 | xargs -I {} mv ${GATLING_REPORT_DIR}{} ${GATLING_REPORT_DIR}report
cp ${GATLING_REPORT_DIR}report/simulation.log $GATHER_REPORTS_DIR


for HOST in "${HOSTS[@]}"
do
  echo "Gathering result file from host: $HOST"
  ssh -n -f -i cloud.key $USER_NAME@$HOST "sh -c 'ls -t $GATLING_REPORT_DIR | head -n 1 | xargs -I {} mv ${GATLING_REPORT_DIR}{} ${GATLING_REPORT_DIR}report'"
  scp $USER_NAME@$HOST:${GATLING_REPORT_DIR}report/simulation.log ${GATHER_REPORTS_DIR}simulation-$HOST.log
done

mv $GATHER_REPORTS_DIR $GATLING_REPORT_DIR
echo "Aggregating simulations"
$GATLING_RUNNER -ro reports