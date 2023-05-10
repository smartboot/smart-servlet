/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package org.smartboot.servlet.testsuite;

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
import org.smartboot.http.common.utils.Mimetypes;
import org.smartboot.http.server.*;
import org.smartboot.http.server.impl.WebSocketRequestImpl;
import org.smartboot.http.server.impl.WebSocketResponseImpl;
import org.smartboot.servlet.ContainerRuntime;
import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.conf.ServletInfo;

import jakarta.servlet.ServletContext;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class JettyEmbeddedContainer implements DeployableContainer<JettyEmbeddedConfiguration> {
    private static final Logger log = Logger.getLogger(JettyEmbeddedContainer.class.getName());

    private HttpBootstrap bootstrap;
    private ContainerRuntime containerRuntime;
    private ArquillianAppProvider appProvider;

    private JettyEmbeddedConfiguration containerConfig;

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
    public Class<JettyEmbeddedConfiguration> getConfigurationClass() {
        return JettyEmbeddedConfiguration.class;
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

    public void setup(JettyEmbeddedConfiguration containerConfig) {
        this.containerConfig = containerConfig;
    }

    public void start() throws LifecycleException {
        appProvider = new ArquillianAppProvider(containerConfig);
        this.bootstrap = new HttpBootstrap();
        containerRuntime = new ContainerRuntime();
        bootstrap.httpHandler(new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) {
                containerRuntime.doHandle(request, response);
            }
        }).webSocketHandler(new WebSocketHandler() {
            @Override
            public void whenHeaderComplete(WebSocketRequestImpl request, WebSocketResponseImpl response) {
                containerRuntime.onHeaderComplete(request.getRequest());
            }

            @Override
            public void handle(WebSocketRequest request, WebSocketResponse response) {
                containerRuntime.doHandle(request, response);
            }
        });
        bootstrap.configuration().bannerEnabled(false).readBufferSize(1024 * 1024).debug(true);

        containerRuntime.start(this.bootstrap.configuration());
        bootstrap.setPort(containerConfig.getBindHttpPort()).start();
        listeningHost = "127.0.0.1";
//        listeningHost = containerConfig.getBindAddress();
        listeningPort = containerConfig.getBindHttpPort();
        System.out.println("host: " + listeningHost + " port:" + listeningPort);
    }

    private String getRealmName() {
        File realmProperties = containerConfig.getRealmProperties();
        String fileName = realmProperties.getName();
        int index;
        if ((index = fileName.indexOf('.')) > -1) {
            fileName = fileName.substring(0, index);
        }
        return fileName;
    }

    public void stop() throws LifecycleException {
        try {
            System.out.println("stop.....");
            containerRuntime.stop();
            bootstrap.shutdown();
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
            ServletContextRuntime app = appProvider.createApp(containerRuntime,archive);

            app.start();

            webAppContextProducer.set(app);
            servletContextInstanceProducer.set(app.getServletContext());

            HTTPContext httpContext = new HTTPContext(listeningHost, listeningPort);
            for (ServletInfo servlet : app.getDeploymentInfo().getServlets().values()) {
                httpContext.add(new Servlet(servlet.getServletName(), app.getContextPath()));
            }
            return new ProtocolMetaData().addContext(httpContext);
        } catch (Exception e) {
            throw new DeploymentException("Could not deploy " + archive.getName(), e);
        }
    }

    private Mimetypes getMimeTypes() {
        Map<String, String> configuredMimeTypes = containerConfig.getMimeTypes();
        Set<Map.Entry<String, String>> entries = configuredMimeTypes.entrySet();
//        MimeTypes mimeTypes = new MimeTypes();
//        entries.forEach(stringStringEntry ->
//                mimeTypes.addMimeMapping(stringStringEntry.getKey(), stringStringEntry.getValue()));
        return Mimetypes.getInstance();
    }

    public void undeploy(Archive<?> archive) throws DeploymentException {
        ServletContextRuntime app = webAppContextProducer.get();
        if (app != null) {
            app.stop();
        }
    }
}
