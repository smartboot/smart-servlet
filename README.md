![](smart-servlet.svg)
# smart-servlet
smart-servlet 是一款实现了 Servlet 4.0 规范，支持多应用隔离部署的的 Web 容器。 与此同时，smart-servlet 还是一款插件化容器，基于内置的沙箱环境确保 smart-servlet 拥有最精简的运行能力，用户还可以通过自定义插件扩展容器的服务能力。

**产品特色**
* 国产血统：核心技术 100% 全栈自研。
* 性能优越：搭载最新版通信微内核 smart-socket。
* 安全可靠：严格遵循协议规范；支持加密传输方式。
* 简洁易用：支持 War 包、springboot、maven-plugin等多种运行模式，使用体验100%兼容 Tomcat。

**目标用户**
1. 有着信创需求的企业用户。
2. 对服务并发能力要求高的企业用户。

> 本项目可用于个人学习，未经授权不得用于商业场景。


**[《使用手册》](https://smartboot.tech/smart-servlet/)**


## 项目推荐
- [smart-socket](https://gitee.com/smartboot/smart-socket)  
    极简、易用、高性能的AIO通信框架，5G时代的通信微内核，适用于IM、RPC、IoT等诸多领域
- [smart-http](https://gitee.com/smartboot/smart-http)  
    基于smart-socket实现的轻量级http服务器
- [smart-mqtt](https://gitee.com/smartboot/smart-mqtt)  
  基于 smart-socket 实现的 MQTT 3.1.1/5.0 Broker&Client 服务。