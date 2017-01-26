#install java
sudo apt-get update
sudo apt --yes install openjdk-8-jdk openjdk-8-demo openjdk-8-doc openjdk-8-jre-headless openjdk-8-source

#Download Gatling
curl -k https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/2.2.3/gatling-charts-highcharts-bundle-2.2.3-bundle.zip -o gatling.zip
unzip gatling.zip -d gatling
cd gatling/gatling-charts-highcharts-bundle-2.2.3

#download formic assembly jars