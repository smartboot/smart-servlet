/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.basic;

import org.smartboot.socket.enhance.EnhanceAsynchronousChannelProvider;
import org.smartboot.socket.extension.plugins.SslPlugin;
import org.smartboot.socket.extension.ssl.factory.PemServerSSLContextFactory;
import org.smartboot.socket.extension.ssl.factory.SSLContextFactory;
import org.smartboot.socket.extension.ssl.factory.ServerSSLContextFactory;
import tech.smartboot.feat.Feat;
import tech.smartboot.feat.cloud.FeatCloud;
import tech.smartboot.feat.core.common.FeatUtils;
import tech.smartboot.feat.core.common.HeaderName;
import tech.smartboot.feat.core.common.HeaderValue;
import tech.smartboot.feat.core.common.exception.FeatException;
import tech.smartboot.feat.core.common.logging.Logger;
import tech.smartboot.feat.core.common.logging.LoggerFactory;
import tech.smartboot.feat.core.server.HttpRequest;
import tech.smartboot.feat.core.server.impl.HttpEndpoint;
import tech.smartboot.feat.core.server.upgrade.http2.Http2Upgrade;
import tech.smartboot.feat.router.Context;
import tech.smartboot.feat.router.Router;
import tech.smartboot.feat.router.RouterHandler;
import tech.smartboot.servlet.Container;
import tech.smartboot.servlet.ContainerConfig;
import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.enums.SslCertType;
import tech.smartboot.servlet.plugins.Plugin;
import tech.smartboot.servlet.util.ParamReflect;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/9/30
 */
public class BasicPlugin extends Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicPlugin.class);
    private String waringMessage = "";
    private Router router;


    @Override
    public void initPlugin(Container container) {
        // springboot工程不依赖 smart-servlet.properties
        if (!isSpringBoot()) {
            try (InputStream fileInputStream = getResource(Container.CONFIGURATION_FILE)) {
                if (fileInputStream == null) {
                    System.err.println("smart-servlet.properties not found");
                } else {
                    ParamReflect.reflect(fileInputStream, container.getConfiguration());
                }
            } catch (IOException e) {
                throw new FeatException(e);
            }
        }
        router = new Router();
    }

    @Override
    public void onContainerInitialized(Container container) {
        ContainerConfig config = container.getConfiguration();

        if (!config.isEnabled() && !config.isSslEnable()) {
            System.err.println(ConsoleColors.RED + "WARN: smart-servlet is disabled, please check the configuration " + "file: " + Container.CONFIGURATION_FILE + ConsoleColors.RESET);
        }
        try {
            AtomicInteger threadSeqNumber = new AtomicInteger();
            AsynchronousChannelGroup group =
                    new EnhanceAsynchronousChannelProvider(false).openAsynchronousChannelGroup(config.getThreadNum(),
                            r -> new Thread(r, "smart-servlet:Thread-" + (threadSeqNumber.getAndIncrement())));
            System.out.println("\033[1mWeb Info:\033[0m");
            if (config.isEnabled()) {
                FeatCloud.cloudServer(options -> {
                    options.setRouter(router).group(group)
                            .readBufferSize(config.getReadBufferSize())
                            .debug(config.isDebugEnable())
                            .headerLimiter(config.getHeaderLimiter())
                            .bannerEnabled(false)
                            .setIdleTimeout(config.getHttpIdleTimeout());
                    config.getPlugins().forEach(options::addPlugin);
                    if (config.isProxyProtocolEnable()) {
                        options.proxyProtocolSupport();
                    }
                }).listen(config.getHost(), config.getPort());
                System.out.println("\tHTTP is enabled, " + config.getHost() + ":" + config.getPort());
            } else {
                System.out.println("\tHTTP is disabled.");
            }
            if (config.isSslEnable()) {
                startSslServer(config, group, router);
            } else {
                System.out.println("\tHTTPS is disabled.");
            }

        } catch (Exception e) {
            LOGGER.error("initPlugin error", e);
            throw new FeatException(e);
        }

        if (FeatUtils.isNotBlank(waringMessage)) {
            System.out.println(ConsoleColors.RED + waringMessage + ConsoleColors.RESET);
            waringMessage = null;
        }
    }

    private void startSslServer(ContainerConfig config, AsynchronousChannelGroup group,
                                Router router) {
        System.out.println("\tTLS enabled, port:" + config.getSslPort());
        SslPlugin<HttpEndpoint> sslPlugin;
        SslCertType type = config.getSslCertType();
        switch (type) {
            case pem:
                try (InputStream pemStream = getResource("smart-servlet.pem")) {
                    if (pemStream != null) {
                        SSLContextFactory sslContextFactory = new PemServerSSLContextFactory(pemStream);
                        sslPlugin = new SslPlugin<>(sslContextFactory, sslEngine -> {
                            sslEngine.setUseClientMode(false);
                            sslEngine.setNeedClientAuth(config.isNeedClientAuth());
                            HttpRequest.SSL_ENGINE_THREAD_LOCAL.set(sslEngine);
                        });
                    } else {
                        System.out.println("\t" + ConsoleColors.RED + "smart-servlet.pem not found, please check the "
                                + "file:[ " + (isSpringBoot() ? "src/main/resources/smart-servlet/smart-servlet.pem"
                                : "${SERVLET_HOME}/conf/smart-servlet.pem") + " ]." + ConsoleColors.RESET);
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("\t" + ConsoleColors.RED + "load smart-servlet.pem exception:" + e.getMessage()
                            + ", please check the file:[ "
                            + (isSpringBoot() ? "src/main/resources/smart-servlet/smart-servlet.pem" : "${SERVLET_HOME}/conf/smart-servlet.pem")
                            + " ]." + ConsoleColors.RESET);
                    return;
                }

                break;
            case jks:
                try (InputStream jksStream = new FileInputStream(config.getSslKeyStore())) {
                    SSLContextFactory sslContextFactory = new ServerSSLContextFactory(jksStream,
                            config.getSslKeyStorePassword(), config.getSslKeyPassword());
                    sslPlugin = new SslPlugin<>(sslContextFactory, sslEngine -> {
                        sslEngine.setUseClientMode(false);
                        sslEngine.setNeedClientAuth(config.isNeedClientAuth());
                        HttpRequest.SSL_ENGINE_THREAD_LOCAL.set(sslEngine);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                break;
            case custom:
                try {
                    sslPlugin = new SslPlugin<>(config.getFactory(), sslEngine -> {
                        sslEngine.setUseClientMode(false);
                        sslEngine.setNeedClientAuth(config.isNeedClientAuth());
                        HttpRequest.SSL_ENGINE_THREAD_LOCAL.set(sslEngine);
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                throw new UnsupportedOperationException("无效证书类型");
        }
        Feat.httpServer(options -> {
            options.group(group)
                    .readBufferSize(config.getSslReadBufferSize())
                    .debug(config.isDebugEnable())
                    .bannerEnabled(false)
                    .setIdleTimeout(config.getHttpIdleTimeout())
                    .addPlugin(sslPlugin);
            config.getPlugins().forEach(options::addPlugin);
            if (config.isProxyProtocolEnable()) {
                options.proxyProtocolSupport();
            }
        }).httpHandler(router).listen(config.getHost(), config.getSslPort());
    }

    @Override
    protected void destroyPlugin(Container container) {
//        container.getConfiguration().group().shutdown();
    }

    @Override
    public void addServletContext(ServletContextRuntime runtime) {
        runtime.setVendorProvider(response -> {
        });
        try {
            Class clazz = runtime.getDeploymentInfo().getClassLoader().loadClass("javax.servlet.Servlet");
            if (clazz != null) {
                if (isSpringBoot()) {
                    waringMessage += "检测到你的工程正在依赖旧版本：javax.servlet规范, 请确保你所使用的 Springboot 版本号高于 3.0\r\n";
                } else {
                    waringMessage += "检测到 " + runtime.getContextPath() + " 正在依赖旧版本：javax.servlet 规范, 请先升级到：jakarta.servlet\r\n";
                }

            }
        } catch (ClassNotFoundException ignore) {
        }
        String contextPath = runtime.getContextPath();
        if (contextPath.endsWith("/")) {
            contextPath += "*";
        } else {
            contextPath += "/*";
        }
        router.route(contextPath, new RouterHandler() {
            @Override
            public void handle(Context ctx) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void handle(Context ctx, CompletableFuture<Void> completableFuture) throws IOException {
                String upgrade = ctx.Request.getHeader(HeaderName.UPGRADE.getName());
                if (HeaderValue.Upgrade.H2C.equalsIgnoreCase(upgrade)) {
                    ctx.Request.upgrade(new Http2Upgrade() {
                        @Override
                        public void handle(HttpRequest request, CompletableFuture<Void> completableFuture) throws Throwable {
                            runtime.getContainerRuntime().doHandle(request, completableFuture, runtime);
                        }
                    });
                } else {
                    runtime.getContainerRuntime().doHandle(ctx.Request, completableFuture, runtime);
                }

            }
        });
    }

    @Override
    public void willStartServletContext(ServletContextRuntime containerRuntime) {
        containerRuntime.setFaviconProvider(runtime -> {
        });
    }
}
