#!/bin/bash

JAVA_OPTS=${JAVA_OPTS:="-Xmx256m"}
${JAVA_HOME}/bin/java $JAVA_OPTS -jar "/usr/local/app/${HASHER_JAR}"
