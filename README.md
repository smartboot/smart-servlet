# smart-servlet

![](https://smartboot.tech/smart-servlet/smart-servlet.svg)

[![Maven Central](https://img.shields.io/maven-central/v/tech.smartboot.servlet/smart-servlet-spring-boot-starter)](https://central.sonatype.com/artifact/tech.smartboot.servlet/smart-servlet-spring-boot-starter)
[![License](https://img.shields.io/badge/License-Apache--2.0-blue)](./LICENSE)
[![JDK](https://img.shields.io/badge/JDK-17%2B-blue)](https://central.sonatype.com/artifact/tech.smartboot.servlet/smart-servlet-spring-boot-starter)

把 Spring Boot 里的 Tomcat，换成 12MB 的 smart-servlet。

**Servlet TCK 通过率 99.9% · 42W QPS（wrk 压测） · 初始 Heap 12MB · Apache-2.0 免费商用**

---

## 为什么换掉 Tomcat

- **内存成本**：初始 Heap 12MB，压测 Heap 峰值 400MB（同一压测中 Tomcat 为 550MB、Undertow 为 750MB），云上按内存计费与容器化部署场景直接降低运行成本
- **自主可控**：核心代码 100% 自主研发、源码完全开放，便于代码审计与安全评估，满足国产化替代选型的合规要求
- **性能余量**：搭载 smart-socket 微内核与高效 IO 模型，wrk 压测 42W QPS，高并发场景下留足余量

## 5 分钟替换

要求 Java 17+，示例版本 `4.0`（请使用[最新版本](https://smartboot.tech/smart-servlet/guides/release_note/)）。业务代码零改动，只改 `pom.xml`：

```xml
<dependencies>
    <!-- 1. 排除 Spring Boot 默认的 Tomcat -->
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

    <!-- 2. 接入 smart-servlet -->
    <dependency>
        <groupId>tech.smartboot.servlet</groupId>
        <artifactId>smart-servlet-spring-boot-starter</artifactId>
        <version>4.0</version>
    </dependency>
</dependencies>
```

也支持 WAR 包部署、Maven 插件启动等运行方式，详见[安装指南](https://smartboot.tech/smart-servlet/guides/install/)。

## 证据

- **规范兼容**：遵循 Jakarta Servlet 6.1，Servlet TCK 通过率 99.9%（1732 项用例中 1731 项通过）→ [TCK 测试报告](https://smartboot.tech/smart-servlet/guides/tck_report/)
- **性能数据**：wrk / ApacheBench 压测对比与测试方法 → [性能基准报告](https://smartboot.tech/smart-servlet/guides/benchmark/)

## 协议

[Apache License 2.0](./LICENSE)，企业免费用于生产与商业场景，无功能墙、无授权风险。付费的只有服务，条款详见[开源协议与使用条款](https://smartboot.tech/smart-servlet/legal/)。

企业服务订阅（答疑咨询、需求支持、LTS 与安全补丁）请邮件联系 `zhengjunweimail@163.com`。

---

## 支持

- **问题反馈（开源生态支持）**：[提交 Issue（Gitee）](https://gitee.com/smartboot/smart-servlet/issues/new?template=opensource_bug.yml) / [GitHub Issue](https://github.com/smartboot/smart-servlet/issues)，由社区响应
- **答疑咨询 / 企业服务订阅**：邮件 `zhengjunweimail@163.com`，1 对 1 联系

---

## 项目推荐

- [smart-socket](https://gitee.com/smartboot/smart-socket) — 极简高性能 AIO 通信框架，适用于 IM、RPC、IoT
- [feat](https://gitee.com/smartboot/feat) — 以 AI 原生和云原生双引擎驱动的现代化 Java Web 服务开发框架
- [smart-mqtt](https://gitee.com/smartboot/smart-mqtt) — 基于 smart-socket 的 MQTT 3.1.1/5.0 Broker & Client 服务

---

📖 **完整文档**：[https://smartboot.tech/smart-servlet/](https://smartboot.tech/smart-servlet/)
