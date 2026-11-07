# smart-servlet

![](./pages/public/smart-servlet.svg)

轻量、高性能的 Servlet 容器，基于 Jakarta Servlet 6.1 规范，面向 Java 17+ 环境。可作为 Tomcat / Undertow 的直接替代方案，无缝运行 Spring Boot、Spring Cloud 等主流框架。

[快速开始](#快速开始) · [性能对比](#性能对比) · [开源协议](#开源协议) · [企业服务](#企业服务订阅) · [在线文档](https://smartboot.tech/smart-servlet/)

---

## 为什么选择 smart-servlet

- **性能优越**：搭载 smart-socket 微内核与高效 IO 模型，单机支持十万级 QPS；内存占用低至 12MB，资源受限环境同样游刃有余
- **规范兼容**：遵循 Jakarta Servlet 6.1，通过 Servlet TCK 兼容性测试；所有遵循 Servlet 规范的框架均可直接运行
- **轻量灵活**：核心设计精简，启动快、依赖少；提供丰富的 SPI 接口与插件机制，支持自定义协议处理、安全策略扩展与性能监控集成
- **自主可控**：核心技术 100% 自主研发，源码完全开放，便于企业进行代码审计与长期维护
- **接入成本低**：兼容 WAR 包、Spring Boot、Maven Plugin 等多种运行方式，现有应用几乎零改动即可切换

---

## 快速开始

要求 Java 17+。三种接入方式，任选其一（示例版本 `3.3`，请使用[最新版本](https://smartboot.tech/smart-servlet/guides/release_note/)）。

### 方式一：Spring Boot 工程

替换 Spring Boot 默认的 Tomcat：

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

```bash
mvn spring-boot:run
```

### 方式二：WAR 包部署

从[下载页面](https://smartboot.tech/smart-servlet/guides/release_note/)获取发行包，将 WAR 包放入 `webapps` 目录后启动：

```bash
tar -xzf smart-servlet-bin-3.3.tar.gz
cp your-app.war smart-servlet-bin-3.3/webapps/
cd smart-servlet-bin-3.3/bin && ./start.sh   # Windows 双击 start.bat
```

### 方式三：Maven 插件启动

```xml
<build>
    <plugins>
        <plugin>
            <groupId>tech.smartboot.servlet</groupId>
            <artifactId>smart-servlet-maven-plugin</artifactId>
            <version>3.3</version>
            <configuration>
                <port>8080</port>
                <path>/</path>
            </configuration>
        </plugin>
    </plugins>
</build>
```

```bash
mvn package smart-servlet:run
```

启动后访问 `http://localhost:8080` 即可。

---

## 性能对比

测试场景：Spring Boot 工程输出纯文本，`ab -n 1000000 -k` 持续压测，数据取自压测服务端（[测试代码](https://github.com/smartboot/smart-servlet/blob/master/springboot-demo/src/main/java/org/smartboot/demo/starter/Bootstrap.java)，可自行复现）。

| 并发数 | Tomcat | Undertow | smart-servlet |
| :---: | :---: | :---: | :---: |
| 10 | 64311 | 74860 | **93812** |
| 100 | 92047 | 89037 | **108844** |
| 1000 | 88997 | 102439 | **111308** |

其他维度（1024 并发、wrk 持续 15 秒）：初始 Heap 内存 12MB（Tomcat 15MB / Undertow 15MB），压测 QPS 42W/s（Tomcat 10W/s / Undertow 40W/s）。

---

## 开源协议

**smart-servlet 采用 [Apache License 2.0](./LICENSE) 开源协议，企业可免费用于生产与商业场景，无需采购使用授权。**

- 允许商用、修改、再分发、嵌入闭源产品、作为托管服务对外提供
- 无并发数、部署规模、功能或试用时长的限制，免费用户与企业服务订阅客户使用同一份代码
- 再分发时需保留版权声明与 NOTICE 文件，遵守 Apache-2.0 的专利授权与商标条款
- 商标、官方认证与兼容性标识不属于 Apache-2.0 授权范围，使用前需获得书面许可

软件按"现状（AS IS）"提供，生产环境使用前建议先完成充分测试与灰度验证。详见[开源协议与使用条款](https://smartboot.tech/smart-servlet/legal/)。

---

## 企业服务订阅

社区版功能完整且免费。付费服务面向在生产环境深度使用、希望获得专业答疑与需求支持的团队，按年度订阅：

| 能力 | 社区版（免费） | 企业服务订阅（付费） |
| :--- | :---: | :---: |
| 使用授权 | Apache-2.0，商用免费 | 相同，无需额外授权 |
| 功能、性能、并发与部署规模 | 完全一致，无限制 | 完全一致 |
| 问题响应 | 公开 Issue 开源生态支持，尽力而为、无 SLA | 1 对 1 答疑咨询（专属对接人／专属群／服务工单），及时响应、跟进到底 |
| 需求支持 | 公开建议渠道，按社区节奏排期 | 功能需求评估、优先排期与定制化方案 |
| 生产故障处置 | 自行处理 | 优先处置、远程/现场支持 |
| 版本维护 | 跟随主干版本 | 指定版本长期维护（LTS）、安全补丁、EOL 提前通知 |
| 大版本升级 | 自助适配 | JDK / Spring Boot / Jakarta Servlet 升级适配指导 |
| 实施与调优 | 自助 | 迁移实施、上线前架构评审、压测与性能调优、培训（按人天） |
| 生态权益 | 无 | 服务客户公示、兼容性认证、生态合作（商标使用需单独授权） |

**计费方式**：按部署规模与支持等级年度订阅；实施、定制开发与驻场按人天计价。具体报价与服务范围欢迎通过商务渠道咨询确认。

订阅服务不改变开源协议，也不会限制未订阅用户使用任何功能。

---

## 支持渠道与服务咨询

**公开 Issue 仅提供开源生态支持**：面向所有免费用户的 Bug 反馈、使用咨询与改进建议，由社区尽力而为，不承诺响应时效，不提供 SLA 与故障兜底。请勿在公开 Issue 提交生产故障、密钥口令、内网拓扑或业务数据。

- [开源生态支持（问题反馈）](https://gitee.com/smartboot/smart-servlet/issues/new?template=opensource_bug.yml)

**企业服务咨询与订阅**：咨询服务内容、报价或申请年度服务订阅，请通过 1 对 1 渠道联系商务，不在公开 Issue 提交。

- 邮箱：`zhengjunweimail@163.com`
- 服务说明与联系方式：[服务客户与生态伙伴](https://smartboot.tech/smart-servlet/users/)

**订阅客户**：请使用专属的 **1 对 1 服务通道**（专属对接人／专属沟通群／服务工单），公开 Issue 不受理订阅客户的报障与 SLA 诉求。

> 建议先完成产品试用并确认满足业务需求，再评估是否需要订阅服务。

---

## 项目推荐

- [smart-socket](https://gitee.com/smartboot/smart-socket) — 极简高性能 AIO 通信框架，适用于 IM、RPC、IoT
- [feat](https://gitee.com/smartboot/feat) — 基于 smart-socket 的轻量级 HTTP 服务器
- [smart-mqtt](https://gitee.com/smartboot/smart-mqtt) — 基于 smart-socket 的 MQTT 3.1.1/5.0 Broker & Client 服务
