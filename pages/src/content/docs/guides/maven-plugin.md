---
title: Maven 插件
sidebar:
  order: 2
---

这是一种类似：`tomcat-maven-plugin`的使用方式，通常应用于 Java Web 工程的本地开发环境。
集成该插件只需在 pom.xml 中加入以下代码，便可以在 IDE 中启动 servlet 服务。
```xml
<build>
    <plugins>
        <plugin>
            <groupId>tech.smartboot.servlet</groupId>
            <artifactId>smart-servlet-maven-plugin</artifactId>
            <version>${最新版本号}</version><!--最新版本 -->
            <configuration>
                <port>8080</port>
                <path>/</path>
            </configuration>
        </plugin>
    </plugins>
</build>
```
插件的版本建议采用最新版本，另外主要的配置项包括：
- port：servlet服务启动的监听端口
- path：Servlet容器上下文路径，即 ContextPath，通常以`/`表示。当然也支持自定义，但必须以`/`开头

完成配置后在控制台输入：`mvn package smart-servlet:run`即可。
