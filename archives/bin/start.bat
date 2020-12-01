@echo off
rem ---------------------------------------------------------------------------
rem Start script for the smart-servlet
rem ---------------------------------------------------------------------------

cd /d %~dp0
cd ..
set "SERVLET_HOME=%cd%"
set BootStrapClass=org.smartboot.servlet.starter.Bootstrap
set "SERVLET_CLASSPATH=%SERVLET_HOME%/lib/"
set "SERVLET_PLUGINS=%SERVLET_HOME%/plugins/"
set "SERVLET_WEBAPPS=%SERVLET_HOME%/webapps"
set "SERVLET_LOG4J=file:%SERVLET_HOME%/conf/log4j2.xml"
java -Dwebapps.dir="%SERVLET_WEBAPPS%" -Dlog4j.configurationFile="%SERVLET_LOG4J%" -Djava.ext.dirs="%JAVA_HOME%/jre/lib/ext;%SERVLET_CLASSPATH%;%SERVLET_PLUGINS%" "%BootStrapClass%"

