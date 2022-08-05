#! /bin/bash

function make_javadoc () {
	mvn -DskipTests=true clean install site:site site:stage
	cp -rf target/stage/apidocs/ mc-config-site/
	mkdir -p mc-config-site/apidocs/resources/fonts/
	touch mc-config-site/apidocs/resources/fonts/dejavu.css
}

make_javadoc
make -C sourcedoc clean html github
git -C mc-config-site status
git -C mc-config-site commit -a -m "update apidocs on `date +%Y-%m-%dT%H:%M:%S%z`"


