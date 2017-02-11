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
        echo "Must provide param for test class"
        exit 1
fi
if [ -z "$2" ]
    then
        echo "Must provide param for number of users"
        exit 1
fi
if [ -z "$3" ]
    then
        echo "Must provide param for data type instance id"
        exit 1
fi


#Assuming same user name for all hosts
USER_NAME='ubuntu'
FORMIC_SERVER="http://10.200.1.67:80"

#Remote hosts list
if [ $2 -eq 0 ]
    then HOSTS=()
else
    HOSTS=( 192.168.0.27 192.168.0.28 192.168.0.29 192.168.0.30 )
fi

#Simulation options
NUM_EDITORS=$2
DATA_TYPE_IDS=( $3 $4 $5 $6 )

#Assuming all Gatling installation in same path (with write permissions)
GATLING_HOME=gatling-charts-highcharts-bundle-2.2.1
GATLING_SIMULATIONS_DIR=$GATLING_HOME/user-files/simulations
GATLING_RUNNER=$GATLING_HOME/bin/gatling.sh

#Change to your simulation class name
SIMULATION_NAME=$1

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
  scp -i cloud.key $GATLING_LIB_DIR/formic-gatling-assembly-1.0.0.jar $USER_NAME@$HOST:$GATLING_LIB_DIR
  scp -i cloud.key $GATLING_LIB_DIR/formic-gatling-test-1.0.0.jar $USER_NAME@$HOST:$GATLING_LIB_DIR
done

for DATA_TYPE_ID in "${DATA_TYPE_IDS[@]}"
do
    workerNumber=1
    for HOST in "${HOSTS[@]}"
    do
      JAVA_OPTS="-DformicEditors=$NUM_EDITORS -DformicId=$DATA_TYPE_ID -DformicServer=$FORMIC_SERVER -DworkerNumber=$workerNumber"
      export JAVA_OPTS
      echo "Running simulation on host: $HOST"
      ssh -n -f -i cloud.key $USER_NAME@$HOST "sh -c 'nohup ./wrapGatlingExecution.sh $JAVA_OPTS $GATLING_RUNNER $SIMULATION_NAME'"
      ((workerNumber+=1))
    done


    JAVA_OPTS="-DformicEditors=$NUM_EDITORS -DformicId=$DATA_TYPE_ID -DformicServer=$FORMIC_SERVER -DworkerNumber=0"
    export JAVA_OPTS
    echo "Running simulation on localhost"
    $GATLING_RUNNER -nr -s $SIMULATION_NAME > gatling-run-localhost-${DATA_TYPE_ID}.log

    echo "Gathering result file from localhost"
    ls -t $GATLING_REPORT_DIR | head -n 1 | xargs -I {} mv ${GATLING_REPORT_DIR}{} ${GATLING_REPORT_DIR}report
    cp ${GATLING_REPORT_DIR}report/simulation.log ${GATHER_REPORTS_DIR}simulation-${DATA_TYPE_ID}.log

    for HOST in "${HOSTS[@]}"
    do
      echo "Gathering result file from host: $HOST"
      ssh -n -f -i cloud.key $USER_NAME@$HOST "sh -c 'ls -t $GATLING_REPORT_DIR | head -n 1 | xargs -I {} mv ${GATLING_REPORT_DIR}{} ${GATLING_REPORT_DIR}report'"
      scp -i cloud.key $USER_NAME@$HOST:${GATLING_REPORT_DIR}report/simulation.log ${GATHER_REPORTS_DIR}simulation-${HOST}-${DATA_TYPE_ID}.log
    done

    for HOST in "${HOSTS[@]}"
    do
      echo "Gathering run log file from host: $HOST"
      scp -i cloud.key $USER_NAME@$HOST:gatling-run.log ./gatling-run-${HOST}-${DATA_TYPE_ID}.log
    done
done

mv $GATHER_REPORTS_DIR $GATLING_REPORT_DIR
echo "Aggregating simulations"
$GATLING_RUNNER -ro reports