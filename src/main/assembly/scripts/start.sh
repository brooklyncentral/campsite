#!/bin/bash
#
# Copyright 2014 by Cloudsoft Corporation Limited
#
#set -x # DEBUG

if [ -z "${JAVA_HOME}" ] ; then
    JAVA=$(which java)
else
    JAVA=${JAVA_HOME}/bin/java
fi

if [ ! -x "${JAVA}" ] ; then
  echo Cannot find java. Set JAVA_HOME or add java to path.
  exit 1
fi

if [ -z "$(ls brooklyn-campsite-*.jar 2> /dev/null)" ] ; then
  echo Command must be run from the directory where the JAR is installed.
  exit 4
fi

${JAVA} -Xms256m -Xmx1024m -XX:MaxPermSize=1024m \
    -classpath "conf/:patch/*:*:lib/*" brooklyn.campsite.CampsiteMain "$@"

