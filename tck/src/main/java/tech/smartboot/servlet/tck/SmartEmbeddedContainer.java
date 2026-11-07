/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.servlet.tck;

import io.github.smartboot.socket.extension.plugins.StreamMonitorPlugin;
import jakarta.servlet.ServletContext;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import tech.smartboot.servlet.Container;
import tech.smartboot.servlet.ContainerConfig;
import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.enums.SslCertType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class SmartEmbeddedContainer implements DeployableContainer<SmartEmbeddedConfiguration> {

    private Container containerRuntime;
    private ArquillianAppProvider appProvider;

    private SmartEmbeddedConfiguration containerConfig;

    private String listeningHost;
    private int listeningPort;

    @Inject
    @DeploymentScoped
    private InstanceProducer<ServletContextRuntime> webAppContextProducer;

    @Inject
    @ApplicationScoped
    private InstanceProducer<ServletContext> servletContextInstanceProducer;

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.spi.client.container
     * .DeployableContainer#getConfigurationClass()
     */
    public Class<SmartEmbeddedConfiguration> getConfigurationClass() {
        return SmartEmbeddedConfiguration.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.spi.client.container
     * .DeployableContainer#getDefaultProtocol()
     */
    public ProtocolDescription getDefaultProtocol() {
        // Jetty 9 is a Servlet 3.1 container.
        // However, Arquillian "Protocol" actuall means "Packaging"
        // TODO: Fix to servlet 3.1 (when available in arquillian)
        return new ProtocolDescription("Servlet 3.0");
    }

    public void setup(SmartEmbeddedConfiguration containerConfig) {
        this.containerConfig = containerConfig;
    }

    public void start() throws LifecycleException {
        appProvider = new ArquillianAppProvider(containerConfig);

        containerRuntime = new Container();

        try {
            containerRuntime.initialize();
        } catch (Throwable e) {
            throw new LifecycleException(e.getMessage(), e);
        }

        ContainerConfig config = containerRuntime.getConfiguration();
        config.setPort(containerConfig.getBindHttpPort()).setReadBufferSize(1024 * 1024).setHttpIdleTimeout(120000).setHost(containerConfig.getBindAddress()).setDefaultAsyncContextTimeout(5000);
        config.getPlugins().add(new StreamMonitorPlugin<>(StreamMonitorPlugin.BLUE_TEXT_INPUT_STREAM, StreamMonitorPlugin.RED_TEXT_OUTPUT_STREAM));

        if (containerConfig.isSsl()) {
            config.setEnabled(false).setSslEnable(true).setNeedClientAuth(containerConfig.isNeedClientAuth()).setSslKeyStore(containerConfig.getKeystorePath()).setSslKeyStorePassword("changeit").setSslKeyPassword("changeit").setSslCertType(SslCertType.jks).setSslPort(containerConfig.getBindHttpPort()).setNeedClientAuth(containerConfig.isNeedClientAuth());
        }
        listeningHost = containerConfig.getBindAddress();
        listeningPort = containerConfig.getBindHttpPort();
        containerRuntime.start();
        System.out.println("host: " + listeningHost + " port:" + listeningPort + " ssl:" + containerConfig.isSsl());
    }


    public void stop() throws LifecycleException {
        try {
            System.out.println("stop.....");
            containerRuntime.stop();
        } catch (Exception e) {
            throw new LifecycleException("Could not stop container", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.spi.client.container
     * .DeployableContainer#deploy(org.jboss.shrinkwrap.descriptor.api
     * .Descriptor)
     */
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.spi.client.container
     * .DeployableContainer#undeploy(org.jboss.shrinkwrap.descriptor.api
     * .Descriptor)
     */
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException {
        try {
            ServletContextRuntime app = appProvider.createApp(containerRuntime, archive);

            loadRealmProperties(app);

            app.start();

            webAppContextProducer.set(app);
            servletContextInstanceProducer.set(app.getServletContext());

            HTTPContext httpContext = new HTTPContext(listeningHost, listeningPort);
            for (ServletInfo servlet : app.getDeploymentInfo().getServlets().values()) {
                httpContext.add(new Servlet(servlet.getServletName(), app.getContextPath()));
            }
            return new ProtocolMetaData().addContext(httpContext);
        } catch (Throwable e) {
            throw new DeploymentException("Could not deploy " + archive.getName(), e);
        }
    }

    public void undeploy(Archive<?> archive) throws DeploymentException {
        ServletContextRuntime app = webAppContextProducer.get();
        if (app != null) {
            app.stop();
        }
    }

    /**
     * 加载 TCK 提供的 realm 用户库（格式：username=password[,role...]）。
     * <p>
     * TCK 默认配置中的证书主体名（DN）到角色的映射依赖该文件，例如 CLIENT-CERT
     * 认证时需要据此把客户端证书身份解析为应用角色。
     */
    private void loadRealmProperties(ServletContextRuntime app) {
        if (!containerConfig.isRealmPropertiesFileSet()) {
            return;
        }
        File realmFile = containerConfig.getRealmProperties();
        if (realmFile == null || !realmFile.isFile()) {
            return;
        }
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(realmFile)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load realm properties from " + realmFile.getAbsolutePath(), e);
        }
        for (String username : properties.stringPropertyNames()) {
            String[] values = properties.getProperty(username).split(",", -1);
            Set<String> roles = new HashSet<>();
            for (int i = 1; i < values.length; i++) {
                if (!values[i].trim().isEmpty()) {
                    roles.add(values[i].trim());
                }
            }
            app.getSecurityProvider().addUser(username, values[0], roles);
        }
    }
}
