/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet;


import org.smartboot.socket.extension.plugins.Plugin;
import org.smartboot.socket.extension.ssl.factory.SSLContextFactory;
import tech.smartboot.feat.core.common.utils.Param;
import tech.smartboot.feat.core.server.impl.HttpEndpoint;
import tech.smartboot.servlet.enums.SslCertType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2020/12/10
 */
public class ContainerConfig {
    private final List<Plugin<HttpEndpoint>> plugins = new ArrayList<>();
    /**
     * http服务端口号
     */
    @Param(name = "http.enable", value = "true")
    private boolean enabled = true;

    @Param(name = "http.debugEnable", value = "false")
    private boolean debugEnable = false;

    @Param(name = "http.proxyProtocolEnable", value = "false")
    private boolean proxyProtocolEnable = false;
    /**
     * http服务端口号
     */
    @Param(name = "http.port", value = "8080")
    private int port = 8080;
    /**
     * http服务端口号
     */
    @Param(name = "http.host", value = "0.0.0.0")
    private String host = "0.0.0.0";
    @Param(name = "http.idleTimeout", value = "60000")
    private int httpIdleTimeout;
    /**
     * 根上下文
     */
    @Param(name = "root.context")
    private String rootContext;
    @Param(name = "http.readBufferSize", value = "1024")
    private int readBufferSize = 1024;


    @Param(name = "http.threadNum")
    private int threadNum = Runtime.getRuntime().availableProcessors() + 1;
    /**
     * SSL是否可用
     */
    @Param(name = "ssl.enable")
    private boolean sslEnable = false;
    /**
     * 证书类型: pem,jks
     */
    @Param(name = "ssl.certType")
    private String sslCertType = "pem";
    @Param(name = "ssl.port")
    private int sslPort = 443;
    @Param(name = "ssl.jks.keystore")
    private String sslKeyStore;
    @Param(name = "ssl.jks.keyStorePassword")
    private String sslKeyStorePassword;
    @Param(name = "ssl.jks.keyPassword")
    private String sslKeyPassword;
    @Param(name = "ssl.readBufferSize")
    private int sslReadBufferSize = 1024;
    /**
     * 是否需要客户端认证
     */
    @Param(name = "ssl.needClientAuth")
    private boolean needClientAuth;
    @Param(name = "virtualThreadEnable")
    private boolean virtualThreadEnable = false;

    private SSLContextFactory factory;

    ContainerConfig() {

    }

    public int getPort() {
        return port;
    }

    public ContainerConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public String getRootContext() {
        return rootContext;
    }

    public void setRootContext(String rootContext) {
        this.rootContext = rootContext;
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public ContainerConfig setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
        return this;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public ContainerConfig setThreadNum(int threadNum) {
        this.threadNum = threadNum;
        return this;
    }

    public boolean isSslEnable() {
        return sslEnable;
    }

    public ContainerConfig setSslEnable(boolean sslEnable) {
        this.sslEnable = sslEnable;
        return this;
    }

    public SslCertType getSslCertType() {
        return SslCertType.getByCode(sslCertType);
    }

    public ContainerConfig setSslCertType(SslCertType sslCertType) {
        this.sslCertType = sslCertType.name();
        return this;
    }

    public int getSslPort() {
        return sslPort;
    }

    public ContainerConfig setSslPort(int sslPort) {
        this.sslPort = sslPort;
        return this;
    }

    public int getSslReadBufferSize() {
        return sslReadBufferSize;
    }

    public ContainerConfig setSslReadBufferSize(int sslReadBufferSize) {
        this.sslReadBufferSize = sslReadBufferSize;
        return this;
    }

    public int getHttpIdleTimeout() {
        return httpIdleTimeout;
    }

    public ContainerConfig setHttpIdleTimeout(int httpIdleTimeout) {
        this.httpIdleTimeout = httpIdleTimeout;
        return this;
    }

    public List<Plugin<HttpEndpoint>> getPlugins() {
        return plugins;
    }

    public String getHost() {
        return host;
    }

    public ContainerConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ContainerConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isVirtualThreadEnable() {
        return virtualThreadEnable;
    }

    public void setVirtualThreadEnable(boolean virtualThreadEnable) {
        this.virtualThreadEnable = virtualThreadEnable;
    }

    public boolean isNeedClientAuth() {
        return needClientAuth;
    }

    public ContainerConfig setNeedClientAuth(boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
        return this;
    }

    public String getSslKeyStore() {
        return sslKeyStore;
    }

    public ContainerConfig setSslKeyStore(String sslKeyStore) {
        this.sslKeyStore = sslKeyStore;
        return this;
    }

    public String getSslKeyStorePassword() {
        return sslKeyStorePassword;
    }

    public ContainerConfig setSslKeyStorePassword(String sslKeyStorePassword) {
        this.sslKeyStorePassword = sslKeyStorePassword;
        return this;
    }

    public String getSslKeyPassword() {
        return sslKeyPassword;
    }

    public ContainerConfig setSslKeyPassword(String sslKeyPassword) {
        this.sslKeyPassword = sslKeyPassword;
        return this;
    }

    public SSLContextFactory getFactory() {
        return factory;
    }

    public void setFactory(SSLContextFactory factory) {
        this.factory = factory;
    }

    public boolean isDebugEnable() {
        return debugEnable;
    }

    public void setDebugEnable(boolean debugEnable) {
        this.debugEnable = debugEnable;
    }

    public boolean isProxyProtocolEnable() {
        return proxyProtocolEnable;
    }

    public void setProxyProtocolEnable(boolean proxyProtocolEnable) {
        this.proxyProtocolEnable = proxyProtocolEnable;
    }
}
