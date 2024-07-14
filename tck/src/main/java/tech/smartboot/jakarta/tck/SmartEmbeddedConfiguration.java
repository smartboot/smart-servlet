/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.jakarta.tck;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.smartboot.http.server.HttpServerConfiguration;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link ContainerConfiguration} common base for the Jetty Embedded
 * containers
 *
 * @author Dan Allen
 * @author Ales Justin
 * @author Alex Soto
 */
public class SmartEmbeddedConfiguration implements ContainerConfiguration {
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

    private String bindAddress = "localhost";

    private int bindHttpPort = 9090;

    private Map<String, String> mimeTypes;

    private Map<String, String> inferredEncodings;

    private int headerBufferSize = 0;

    private File realmProperties;

    /**
     * List of server configuration classes that can be used for
     * establishing the configuration tasks for the WebApp being deployed.
     */
    private String configurationClasses;

    private String requestCookieCompliance;

    private String responseCookieCompliance;

    private boolean useArchiveNameAsContext;

    private boolean ssl;

    private boolean h2cEnabled;

    /**
     * Path to keystore file
     */
    private String keystorePath;

    private String keystorePassword;

    private String trustStorePath;

    private String trustStorePassword;

    private boolean sniRequired;

    private boolean sniHostCheck;

    private boolean needClientAuth;

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.spi.client.container.ContainerConfiguration#validate()
     */
    public void validate() throws ConfigurationException {
        if (this.realmProperties != null) {
            if (!this.realmProperties.exists()) {
                throw new ConfigurationException(
                        String.format("Realm properties file %s must exists.", this.realmProperties.getAbsolutePath()));
            }
            if (this.realmProperties.isDirectory()) {
                throw new ConfigurationException("Realm Properties should be a file and not a directory");
            }
        }
    }

    public int getBindHttpPort() {
        return bindHttpPort;
    }

    public void setBindHttpPort(int bindHttpPort) {
        this.bindHttpPort = bindHttpPort;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public String getConfigurationClasses() {
        return configurationClasses;
    }

    /**
     * @param configurationClasses A comma separated list of fully qualified configuration classes
     */
    public void setConfigurationClasses(String configurationClasses) {
        this.configurationClasses = configurationClasses;
    }

    public int getHeaderBufferSize() {
        return this.headerBufferSize;
    }

    public boolean isHeaderBufferSizeSet() {
        return this.headerBufferSize > 0;
    }

    public void setHeaderBufferSize(int headerBufferSize) {
        this.headerBufferSize = headerBufferSize;
    }

    public void setRealmProperties(String realmProperties) {
        this.realmProperties = new File(realmProperties);
    }

    public boolean isRealmPropertiesFileSet() {
        return this.realmProperties != null;
    }

    public File getRealmProperties() {
        return realmProperties;
    }

    public void setMimeTypes(String mimeTypes) {
        this.mimeTypes = new HashMap<>();
        String[] splittedLines = mimeTypes.split(" ");
        for (int i = 0; i < splittedLines.length; i += 2) {
            if (i + 1 >= splittedLines.length) {
                throw new ConfigurationException(String.format(
                        "Mime Type definition should follow the format <extension> <type>[ <extension> <type>]*, for example js application/javascript but %s definition has been found.",
                        mimeTypes));
            }
            this.mimeTypes.put(splittedLines[i], splittedLines[i + 1]);
        }
    }

    public boolean areMimeTypesSet() {
        return this.mimeTypes != null;
    }

    public Map<String, String> getMimeTypes() {
        return mimeTypes;
    }

    public String getRequestCookieCompliance() {
        return requestCookieCompliance;
    }

    public void setRequestCookieCompliance(String requestCookieCompliance) {
        this.requestCookieCompliance = requestCookieCompliance;
    }

    public String getResponseCookieCompliance() {
        return responseCookieCompliance;
    }

    public void setResponseCookieCompliance(String responseCookieCompliance) {
        this.responseCookieCompliance = responseCookieCompliance;
    }

    public boolean isUseArchiveNameAsContext() {
        return useArchiveNameAsContext;
    }

    public void setUseArchiveNameAsContext(boolean useArchiveNameAsContext) {
        this.useArchiveNameAsContext = useArchiveNameAsContext;
    }

    public void setInferredEncodings(String inferredEncodings) {
        this.inferredEncodings = new HashMap<>();
        String[] splittedLines = inferredEncodings.split(" ");
        for (int i = 0; i < splittedLines.length; i += 2) {
            if (i + 1 >= splittedLines.length) {
                throw new ConfigurationException(String.format(
                        "Mime Type definition should follow the format <extension> <type>[ <extension> <type>]*, for example js application/javascript but %s definition has been found.",
                        inferredEncodings));
            }
            this.inferredEncodings.put(splittedLines[i], splittedLines[i + 1]);
        }
    }

    public boolean areInferredEncodings() {
        return this.inferredEncodings != null;
    }

    public Map<String, String> getInferredEncodings() {
        return inferredEncodings;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public boolean isSniRequired() {
        return sniRequired;
    }

    public void setSniRequired(boolean sniRequired) {
        this.sniRequired = sniRequired;
    }

    public boolean isSniHostCheck() {
        return sniHostCheck;
    }

    public void setSniHostCheck(boolean sniHostCheck) {
        this.sniHostCheck = sniHostCheck;
    }

    public boolean isNeedClientAuth() {
        return needClientAuth;
    }

    public void setNeedClientAuth(boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }

    public boolean isH2cEnabled() {
        return h2cEnabled;
    }

    public void setH2cEnabled(boolean h2cEnabled) {
        this.h2cEnabled = h2cEnabled;
    }
}

