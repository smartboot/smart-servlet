# smart-servlet
smart-servlet 是一款实现了Servlet 3.1规范，支持多应用隔离部署的的 Web 容器。
除此之外，smart-servlet 还是一款插件化容器，基于内置的沙箱环境确保 smart-servlet 拥有最精简的运行能力，用户还可以通过自定义插件扩展容器的服务能力。


<img src="smart-servlet-servlet-container.png" height="50%" width="50%"/>

## Part 1. 项目概述

smart-servlet 在 smart-http 的架构之上，通过继承 HttpHandle 实现了 Servlet 规范。这意味着任何 smart-http 服务都可以通过单独引入 smart-servlet 核心包的方式，将普通的 http 应用改造成 servlet 应用，而且这个成本是极低的。

![](https://foruda.gitee.com/images/1666446010203449163/fca34841_351975.png)

**产品特色**
- 国产血统：核心技术 100% 全栈自研。
- 性能优越：搭载最新版通信微内核 smart-socket。
- 安全可靠：严格遵循协议规范；支持加密传输方式。
- 简洁易用：支持 War 包、springboot、maven-plugin等多种运行模式，使用体验100%兼容 Tomcat。

### 工程模块

- smart-servlet 【工程主目录】
  - servlet-core【servlet规范实现核心包】
  - plugins【容器可扩展插件】
    - dispatcher【RequestDispatcher插件，**必选**】
    - session【HttpSession插件，**可选**】
    - websocket【 JSR 356 规范插件，**可选**】
  - smart-servlet-maven-plugin【业务系统通过pom.xml集成本地开发环境】
  - spring-boot-start【springboot业务系统通过pom.xml集成本地开发环境】

## Part 2. 操作手册


### 2.1 示例演示

1. 下载最新版发行包：https://gitee.com/smartboot/smart-servlet/releases

2. 解压安装包，启动服务：`smart-servlet-bin-x.x.x/bin/start.sh`

3. 若启动过程无任何异常，打开浏览器访问 [http://127.0.0.1:8080/](http:127.0.0.1:8080/)。

   

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
         <version>0.4</version>
         <configuration>
           <port>8080</port>
         </configuration>
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
         <version>0.4</version>
       </dependency>
     </dependencies>
   </project>
  ```

### 代码贡献者

[三刀](https://gitee.com/smartdms)、[**@cwq108**](https://gitee.com/cwq108)、[**@slef**](https://gitee.com/slef)

## 项目推荐
- [smart-socket](https://gitee.com/smartboot/smart-socket)  
    极简、易用、高性能的AIO通信框架，5G时代的通信微内核，适用于IM、RPC、IoT等诸多领域
- [smart-http](https://gitee.com/smartboot/smart-http)  
    基于smart-socket实现的轻量级http服务器