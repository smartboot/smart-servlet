#!/bin/sh
SERVLET_HOME=$(dirname $(pwd))
BootStrapClass=org.smartboot.servlet.starter.Bootstrap
SERVLET_CLASSPATH=${SERVLET_HOME}/lib/
SERVLET_PLUGINS=${SERVLET_HOME}/plugins/
SERVLET_WEBAPPS=${SERVLET_HOME}/webapps
SERVLET_LOG4J=file:${SERVLET_HOME}/conf/log4j2.xml
java -Dwebapps.dir=${SERVLET_WEBAPPS} -Dlog4j.configurationFile=${SERVLET_LOG4J} -Djava.ext.dirs=${JAVA_HOME}/jre/lib/ext:${SERVLET_CLASSPATH}:${SERVLET_PLUGINS} ${BootStrapClass}
