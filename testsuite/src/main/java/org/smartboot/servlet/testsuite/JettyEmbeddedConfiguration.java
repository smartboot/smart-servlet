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


import org.smartboot.http.server.HttpServerConfiguration;

import java.io.File;
import java.net.URI;

/**
 * A {@link org.jboss.arquillian.container.spi.client.container.ContainerConfiguration} implementation for the Jetty Embedded
 * containers.
 *
 * @author Dan Allen
 * @author Ales Justin
 */
public class JettyEmbeddedConfiguration extends AbstractJettyEmbeddedConfiguration {
    public enum ClassLoaderBehavior {
        /**
         * Default behavior for Java Spec (server classloader, then webapp).
         * <p>
         * Also the default for Arquillian.
         */
        JAVA_SPEC,
        /**
         * Default behavior for Servlet Spec (webapp classloader, then server)
         */
        SERVLET_SPEC
    }

    /**
     * Classloader Search Order behavior.
     * <p>
     */
    private ClassLoaderBehavior classloaderBehavior = ClassLoaderBehavior.JAVA_SPEC;

    /**
     * Optional override for the default servlet spec descriptor
     */
    private URI defaultsDescriptor;

    /**
     * Dump, to System.err, the server state tree after the server has successfully started up.
     */
    private boolean dumpServerAfterStart = false;

    /**
     * Optional HttpConfiguration for the ServerConnector that Arquillian
     * creates.
     */
    private HttpServerConfiguration httpConfiguration;

    /**
     * Idle Timeout (in milliseconds) for active connections.
     * <p>
     * Default: 30,000ms
     */
    private long idleTimeoutMillis = 30000;

    /**
     * Base directory for all temp files that Jetty will manage.
     */
    private File tempDirectory;

    public ClassLoaderBehavior getClassloaderBehavior() {
        return classloaderBehavior;
    }

    public URI getDefaultsDescriptor() {
        return defaultsDescriptor;
    }

    public HttpServerConfiguration getHttpConfiguration() {
        return httpConfiguration;
    }

    public long getIdleTimeoutMillis() {
        return idleTimeoutMillis;
    }

    public File getTempDirectory() {
        return tempDirectory;
    }

    public boolean hasDefaultsDescriptor() {
        return (defaultsDescriptor != null);
    }

    public boolean isDumpServerAfterStart() {
        return dumpServerAfterStart;
    }

    public void setClassloaderBehavior(ClassLoaderBehavior classloaderBehavior) {
        this.classloaderBehavior = classloaderBehavior;
    }

    public void setDefaultsDescriptor(URI defaultsDescriptor) {
        this.defaultsDescriptor = defaultsDescriptor;
    }

    public void setDumpServerAfterStart(boolean serverDumpAfterStart) {
        this.dumpServerAfterStart = serverDumpAfterStart;
    }

    public void setHttpConfiguration(HttpServerConfiguration httpConfiguration) {
        this.httpConfiguration = httpConfiguration;
    }

    public void setIdleTimeoutMillis(long milliseconds) {
        this.idleTimeoutMillis = milliseconds;
    }

    public void setTempDirectory(File tempDirectory) {
        this.tempDirectory = tempDirectory;
    }
}
