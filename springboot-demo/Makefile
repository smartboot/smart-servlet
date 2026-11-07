JAVA_HOME=/Users/zhengjunwei/Library/Java/JavaVirtualMachines/azul-26.0.1/Contents/Home
build:
	mvn -f pom.xml clean package
	mv target/*.jar smart-servlet.jar
	mvn -f pom_tomcat.xml clean package
	mv target/*.jar tomcat.jar
	mvn -f pom_undertow.xml clean package
	mv target/*.jar undertow.jar
