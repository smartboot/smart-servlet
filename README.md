# smart-servlet

![](https://smartboot.tech/smart-servlet/smart-servlet.svg)

轻量、高性能的 Servlet 容器，基于 Jakarta Servlet 6.1 规范，面向 Java 17+ 环境。可作为 Tomcat / Undertow 的直接替代方案，无缝运行 Spring Boot、Spring Cloud 等主流框架。

📖 **完整文档**：[https://smartboot.tech/smart-servlet/](https://smartboot.tech/smart-servlet/)

---

## 核心特性

- **性能优越**：搭载 smart-socket 微内核与高效 IO 模型，单机支持十万级 QPS，内存占用低至 12MB
- **规范兼容**：遵循 Jakarta Servlet 6.1，通过 Servlet TCK 兼容性测试
- **轻量灵活**：精简设计，启动快、依赖少，丰富的 SPI 扩展机制
- **自主可控**：核心技术 100% 自主研发，源码开放，便于代码审计

性能压测数据与 TCK 测试报告见 [性能测试报告](https://smartboot.tech/smart-servlet/)。

---

## 快速开始

要求 Java 17+，示例版本 `3.3`（请使用[最新版本](https://smartboot.tech/smart-servlet/guides/release_note/)）。

在 Spring Boot 工程中替换默认的 Tomcat：

```xml
<dependencies>
    <!-- 排除 Spring Boot 默认的 Tomcat -->
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

    <!-- 接入 smart-servlet -->
    <dependency>
        <groupId>tech.smartboot.servlet</groupId>
        <artifactId>smart-servlet-spring-boot-starter</artifactId>
        <version>3.3</version>
    </dependency>
</dependencies>
```

也支持 WAR 包部署、Maven 插件启动等运行方式，详见[安装指南](https://smartboot.tech/smart-servlet/guides/install/)。

---

## 开源协议

**采用 [Apache License 2.0](./LICENSE)，企业可免费用于生产与商业场景，无需采购使用授权**，无并发数、部署规模或功能限制。再分发时需保留版权声明与 NOTICE 文件。详见[开源协议与使用条款](https://smartboot.tech/smart-servlet/legal/)。

---

## 企业服务

社区版功能完整且免费，付费服务不改变开源协议、不解锁任何功能。付费购买的是**答疑咨询与需求支持**，不是使用许可：

- 1 对 1 答疑咨询（专属对接人／专属群／服务工单），及时响应、跟进到底
- 功能需求评估、优先排期与定制化方案
- 生产故障优先处置、指定版本长期维护（LTS）与安全补丁
- JDK / Spring Boot 大版本升级适配指导

服务咨询与报价请邮件联系 `zhengjunweimail@163.com`。

---

## 支持

- **问题反馈（开源生态支持）**：[提交 Issue](https://gitee.com/smartboot/smart-servlet/issues/new?template=opensource_bug.yml)，社区尽力而为，不承诺响应时效
- **答疑咨询 / 企业服务订阅**：邮件 `zhengjunweimail@163.com`，1 对 1 联系

---

## 项目推荐

- [smart-socket](https://gitee.com/smartboot/smart-socket) — 极简高性能 AIO 通信框架，适用于 IM、RPC、IoT
- [feat](https://gitee.com/smartboot/feat) — 基于 smart-socket 的轻量级 HTTP 服务器
- [smart-mqtt](https://gitee.com/smartboot/smart-mqtt) — 基于 smart-socket 的 MQTT 3.1.1/5.0 Broker & Client 服务
