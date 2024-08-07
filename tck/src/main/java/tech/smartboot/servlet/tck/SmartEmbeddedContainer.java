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
import org.smartboot.http.common.codec.websocket.CloseReason;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.WebSocketHandler;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.impl.WebSocketRequestImpl;
import org.smartboot.http.server.impl.WebSocketResponseImpl;
import org.smartboot.socket.extension.plugins.SslPlugin;
import org.smartboot.socket.extension.ssl.ClientAuth;
import org.smartboot.socket.extension.ssl.factory.ServerSSLContextFactory;
import tech.smartboot.servlet.Container;
import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.provider.WebsocketProvider;

import java.io.FileInputStream;
import java.util.concurrent.CompletableFuture;

public class SmartEmbeddedContainer implements DeployableContainer<SmartEmbeddedConfiguration> {

    private HttpBootstrap bootstrap;
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
        this.bootstrap = new HttpBootstrap();
        containerRuntime = new Container();
        bootstrap.httpHandler(new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, CompletableFuture<Object> completableFuture) {
                containerRuntime.doHandle(request, response, completableFuture);
            }
        }).webSocketHandler(new WebSocketHandler() {
            @Override
            public void whenHeaderComplete(WebSocketRequestImpl request, WebSocketResponseImpl response) {
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                try {
                    containerRuntime.doHandle(request, response, completableFuture);
                } finally {
                    if (request.getAttachment() == null || request.getAttachment().get(WebsocketProvider.WEBSOCKET_SESSION_ATTACH_KEY) == null) {
                        response.close(CloseReason.UNEXPECTED_ERROR, "");
                    }
                }
            }

            @Override
            public void handle(WebSocketRequest request, WebSocketResponse response) {
                containerRuntime.doHandle(request, response);
            }
        });
        bootstrap.configuration().bannerEnabled(false).readBufferSize(1024 * 1024).debug(true);

        try {
            containerRuntime.start(this.bootstrap.configuration());
        } catch (Throwable e) {
            throw new LifecycleException(e.getMessage(), e);
        }
        if (containerConfig.isSsl()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(containerConfig.getTrustStorePath());
                bootstrap.configuration().addPlugin(new SslPlugin<>(new ServerSSLContextFactory(fileInputStream, containerConfig.getTrustStorePassword(), containerConfig.getTrustStorePassword()), ClientAuth.NONE));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        bootstrap.setPort(containerConfig.getBindHttpPort()).start();
        listeningHost = "127.0.0.1";
//        listeningHost = containerConfig.getBindAddress();
        listeningPort = containerConfig.getBindHttpPort();
        System.out.println("host: " + listeningHost + " port:" + listeningPort + " ssl:" + containerConfig.isSsl());
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
