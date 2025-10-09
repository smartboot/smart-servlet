# 当需要升级版本时，执行该命令
version=3.1-SNAPSHOT
update_version:
	sed -i  '' 's/public static final String VERSION = ".*";/public static final String VERSION = "v${version}";/' servlet-core/src/main/java/tech/smartboot/servlet/Container.java
	mvn versions:set -DnewVersion=${version} versions:commit clean install -DskipTests
	mvn -f springboot-demo/pom.xml versions:set -DnewVersion=${version} versions:commit
	mvn -f tck/pom.xml versions:set -DnewVersion=${version} versions:commit

