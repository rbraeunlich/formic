#!/usr/bin/env bash
sudo apt-get update
#install necessary packages
echo "Install Java"
sudo DEBIAN_FRONTEND=noninteractive apt-get --yes install openjdk-8-jdk openjdk-8-demo openjdk-8-doc openjdk-8-jre-headless openjdk-8-source
echo "Install Unzip"
sudo DEBIAN_FRONTEND=noninteractive apt-get --yes install unzip

#Download Gatling
echo "Download Gatling"
curl -k https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/2.2.3/gatling-charts-highcharts-bundle-2.2.3-bundle.zip -o gatling.zip
unzip gatling.zip
cd gatling-charts-highcharts-bundle-2.2.3

#download formic assembly jars