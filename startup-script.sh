cd ~
rm -r -f Shepherd-API
git clone https://github.com/dylanturn/Shepherd-API.git
cd ~/Shepherd-API/
mvn clean install
java -Dlog4j.configurationFile=log4j2.xml -cp "target/shepherd-api-1.0-jar-with-dependencies.jar"  dylanturn.shepherdapi.testcluster.TestCluster