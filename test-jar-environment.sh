#! /bin/bash

artifacts=( "mc-config-tool-jar-test" "mc-config-tool-jar-jar-test" )

# use maven to compile artifacts for testing
for artifact in ${artifacts[@]}
do
    mvn -f ${artifact} clean install
done


# NOTE: Put expected Java Version to PATH
for artifact in ${artifacts[@]}
do
    java -jar ${artifact}/target/${artifact}-jar-with-dependencies.jar
done

