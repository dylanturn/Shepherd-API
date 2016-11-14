mkdir ~/shepherdapi
cp log4j2.xml ~/shepherdapi/
cp shepherd-fork-stacks.xml ~/shepherdapi/
cp udp.xml ~/shepherdapi/
cp target/shepherd-api-1.0-jar-with-dependencies.jar ~/shepherdapi/
java -Dlog4j.configurationFile=log4j2.xml -cp "shepherd-api-1.0-jar-with-dependencies.jar"  dylanturn.shepherdapi.testcluster.TestCluster