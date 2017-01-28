#!/usr/bin/env bash
sudo apt-get update
#install necessary packages
echo "Install Java"
sudo DEBIAN_FRONTEND=noninteractive apt-get --yes install openjdk-8-jdk openjdk-8-demo openjdk-8-doc openjdk-8-jre-headless openjdk-8-source
echo "Install Unzip"
sudo DEBIAN_FRONTEND=noninteractive apt-get --yes install unzip

GATLING_VER=2.2.1
#Download Gatling
echo "Download Gatling"
cd /home/ubuntu
curl -k https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/$GATLING_VER/gatling-charts-highcharts-bundle-$GATLING_VER-bundle.zip -o gatling.zip
unzip gatling.zip
sudo chmod -R 777 gatling-charts-highcharts-bundle-$GATLING_VER/
#delete akka JAR because we need a newer version
#the newer version is bundled with the formic test jar
rm gatling-charts-highcharts-bundle-$GATLING_VER/lib/akka-actor_2.11-2.4.4.jar