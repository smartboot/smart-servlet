# smart-servlet
smart-servlet 是一款实现了Servlet 3.1规范，支持多应用隔离部署的的 Web 容器。除此之外，smart-servlet 还是一款插件化容器，基于内置的沙箱环境确保 smart-servlet 拥有最精简的运行能力，用户还可以通过自定义插件扩展容器的服务能力。

<img src="smart-servlet-servlet-container.png" height="50%" width="50%"/>

## Part 1. 项目概述

smart-servlet 在 smart-http 的架构之上，通过继承 HttpHandle 实现了 Servlet 规范。这意味着任何 smart-http 服务都可以通过单独引入 smart-servlet 核心包的方式，将普通的 http 应用改造成 servlet 应用，而且这个成本是极低的。

![](smart-servlet.png)

**产品特色**
- 零学习成本，素未谋面，却已相知。保留用户原有的 Tomcat、Undertow 使用习惯。
- 朴实无华，用最初级的设计手法打造专业级服务器。
- 实现 Servlet 核心规范：request、response、session、cookie、dispatcher、servletContext。
- 插件化设计，自由 DIY 服务器。
- 延续一贯的极简、易用、高性能设计风格。
- 开箱即用，运行程序包、maven本地开发/调试插件、springboot starter 一应俱全，满足你的开发、部署需求。

**有所不为**

有些规范我们默认不会提供实现方案，毕竟时代不一样了。
很多东西在当下已经过时或有了更好的替代方案，我们认为是时候跟过去告别了。
- JNDI
- Security Role
- JSP

### 工程模块

- smart-servlet 【工程主目录】
  - servlet-core【servlet规范实现核心包】
  - plugins【容器可扩展插件】
    - dispatcher【RequestDispatcher插件，**必选**】
    - session【HttpSession插件，**可选**】
    - websocket【 JSR 356 规范插件，**可选**】
  - smart-servlet-maven-plugin【业务系统通过pom.xml集成本地开发环境】
  - spring-boot-start【springboot业务系统通过pom.xml集成本地开发环境】
  - archives【用于部署War包的可执行环境软件包，开箱即用】

## Part 2. 操作手册

### 准备工作

smart-servlet 还未正式发布，如需体验请从本仓库下载源码并导入 IDE 完成工程编译，编译执行顺序如下：

1. 路径：smart-servlet/pom.xml，执行`mvn install`。当控制台出现以下信息时，说明编译成功。

   ```she
   [INFO] ------------------------------------------------------------------------
   [INFO] Reactor Summary:
   [INFO] 
   [INFO] smart-servlet-parent ............................... SUCCESS [  1.168 s]
   [INFO] servlet-core ....................................... SUCCESS [ 10.142 s]
   [INFO] smart-servlet-spring-boot-starter .................. SUCCESS [  2.107 s]
   [INFO] smart-servlet-maven-plugin ......................... SUCCESS [  6.330 s]
   [INFO] ------------------------------------------------------------------------
   [INFO] BUILD SUCCESS
   [INFO] ------------------------------------------------------------------------
   ```

2. 路径：smart-servlet/plugins/pom.xml，执行`mvn install`。当控制台出现以下信息时，说明编译成功。

   ```she
   [INFO] ------------------------------------------------------------------------
   [INFO] Reactor Summary for servlet-plugins-parent 0.1.3-SNAPSHOT:
   [INFO] 
   [INFO] servlet-plugins-parent ............................. SUCCESS [  2.403 s]
   [INFO] plugin-session ..................................... SUCCESS [  3.104 s]
   [INFO] plugin-dispatcher .................................. SUCCESS [  2.005 s]
   [INFO] ------------------------------------------------------------------------
   [INFO] BUILD SUCCESS
   [INFO] ------------------------------------------------------------------------
   ```

3. 路径：smart-servlet/archives/pom.xml，执行`mvn install`。当控制台出现以下信息时，说明编译成功。

   ```shell
   [INFO] ------------------------------------------------------------------------
   [INFO] BUILD SUCCESS
   [INFO] ------------------------------------------------------------------------
   ```

   

### 2.1 示例演示

> 特别说明：smart-servlet 提供的演示文件来自 Tomcat 的示例，存放于`smart-servlet/archives/webapps`目录下。

1. 完成前面的工程编译后，运行archives模块中的`org.smartboot.servlet.starter.Bootstrap`启动服务器。

2. 若启动过程无任何异常，打开浏览器访问 [http://127.0.0.1:8080/examples](http:127.0.0.1:8080/examples)。

   

### 2.2 业务系统集成smart-servlet

根据业务工程实际情况选择相应的集成方式。

- maven plugin

  适用于传统的 Servlet 或者 Spring MVC 工程，且必须是 maven 工程。需要在 web 模块所在的 pom.xml 中加入以下配置，若存在端口冲突自行调整。完成配置后通过：`mvn smart-servlet:run` 启动服务。
  ```xml
  <!-- pom.xml -->
  <project>
   <build>
     <plugins>
       <plugin>
         <groupId>org.smartboot.servlet</groupId>
         <artifactId>smart-servlet-maven-plugin</artifactId>
         <version>0.1.3-SNAPSHOT</version>
         <configuration>
           <port>8080</port>
         </configuration>
         <dependencies>
           <dependency>
             <groupId>org.smartboot.servlet</groupId>
             <artifactId>plugin-session</artifactId>
             <version>0.1.3-SNAPSHOT</version>
           </dependency>
           <dependency>
             <groupId>org.smartboot.servlet</groupId>
             <artifactId>plugin-dispatcher</artifactId>
             <version>0.1.3-SNAPSHOT</version>
           </dependency>
         </dependencies>
       </plugin>
     </plugins>  
   </build>
  </project>
  ```

- springboot starter

  对于 Springboot 提供的集成方式，替换原 spring-boot-starter-web 默认绑定的 Servlet 容器。

  ```xml
   <!-- pom.xml -->
   <project>
     <dependencies>
       <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-web</artifactId>
         <exclusions>
           <exclusion>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter-tomcat</artifactId>
           </exclusion>
         </exclusions>
       </dependency>
       <dependency>
         <groupId>org.smartboot.servlet</groupId>
         <artifactId>smart-servlet-spring-boot-starter</artifactId>
         <version>0.1.3-SNAPSHOT</version>
       </dependency>
     </dependencies>
   </project>
  ```





## 特别说明

本项目还处于研发阶段，还未完成所有 Servlet 标准的兑现。如若在使用过程中发现问题可提 [Issues](https://gitee.com/smartboot/smart-servlet/issues) 反馈，我们会尽快安排处理，感谢您的理解和支持！

QQ交流社群：1167545865（付费制），入群福利：

1. 前20名免费入群。
2. 掌握 smart-servlet 的第一手资讯。
3. 深入探讨 Servlet 规范及服务器开发经验。
4. 与同类开发人群成为朋友。

### 代码贡献者

[三刀](https://gitee.com/smartdms)、[**@cwq108**](https://gitee.com/cwq108)、[**@slef**](https://gitee.com/slef)

## 项目推荐
- [smart-socket](https://gitee.com/smartboot/smart-socket)  
    极简、易用、高性能的AIO通信框架，5G时代的通信微内核，适用于IM、RPC、IoT等诸多领域
- [smart-http](https://gitee.com/smartboot/smart-http)  
    基于smart-socket实现的轻量级http服务器