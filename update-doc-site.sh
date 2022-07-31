#! /bin/bash

mvn -DskipTests=true clean install site:site site:stage site:deploy
cp -rf target/site/apidocs/ mc-config-site/
mkdir -p mc-config-site/apidocs/resources/fonts/
touch mc-config-site/apidocs/resources/fonts/dejavu.css

make -C sourcedoc github
#git -C mc-config-site status
#git -C mc-config-site commit -a -m "update apidocs on `date +%Y-%m-%dT%H:%M:%S%z`"


