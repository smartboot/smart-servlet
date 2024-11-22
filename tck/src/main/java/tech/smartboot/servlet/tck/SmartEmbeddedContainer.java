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
import org.smartboot.socket.extension.plugins.StreamMonitorPlugin;
import tech.smartboot.servlet.Container;
import tech.smartboot.servlet.ContainerConfig;
import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.conf.ServletInfo;

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
     * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getConfigurationClass()
     */
    public Class<SmartEmbeddedConfiguration> getConfigurationClass() {
        return SmartEmbeddedConfiguration.class;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getDefaultProtocol()
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
        config.setPort(containerConfig.getBindHttpPort());
        config.setReadBufferSize(1024 * 1024);
        config.setHttpIdleTimeout(120000);
        config.setHost(containerConfig.getBindAddress());
        config.getPlugins().add(new StreamMonitorPlugin<>(StreamMonitorPlugin.BLUE_TEXT_INPUT_STREAM, StreamMonitorPlugin.RED_TEXT_OUTPUT_STREAM));

        if (containerConfig.isSsl()) {
            config.setEnabled(false);
            config.setSslEnable(true);
            config.setNeedClientAuth(containerConfig.isNeedClientAuth());
            config.setSslKeyStore(containerConfig.getKeystorePath());
            config.setSslKeyStorePassword("changeit");
            config.setSslKeyPassword("changeit");
            config.setSslCertType("jks");
            config.setSslPort(containerConfig.getBindHttpPort());
            config.setNeedClientAuth(containerConfig.isNeedClientAuth());
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
     * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
     */
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
     */
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException {
        try {
            ServletContextRuntime app = appProvider.createApp(containerRuntime, archive);

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
}
