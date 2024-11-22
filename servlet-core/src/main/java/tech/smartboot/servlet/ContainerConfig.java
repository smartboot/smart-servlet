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


import org.smartboot.http.common.utils.Param;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.extension.plugins.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2020/12/10
 */
public class ContainerConfig {
    /**
     * http服务端口号
     */
    @Param(name = "http.enable", value = "true")
    private boolean enabled = true;
    /**
     * http服务端口号
     */
    @Param(name = "http.port", value = "8080")
    private int port;

    /**
     * http服务端口号
     */
    @Param(name = "http.host", value = "0.0.0.0")
    private String host;

    @Param(name = "http.idleTimeout", value = "60000")
    private int httpIdleTimeout;
    /**
     * 根上下文
     */
    @Param(name = "root.context")
    private String rootContext;

    @Param(name = "http.readBufferSize", value = "1024")
    private int readBufferSize = 1024;

    @Param(name = "http.threadNum", value = "4")
    private int threadNum = Runtime.getRuntime().availableProcessors();

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

    @Param(name = "ssl.readBufferSize")
    private int sslReadBufferSize = 1024;

    @Param(name = "virtualThreadEnable")
    private boolean virtualThreadEnable = false;

    private final List<Plugin<Request>> plugins = new ArrayList<>();

    ContainerConfig() {

    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public boolean isSslEnable() {
        return sslEnable;
    }

    public void setSslEnable(boolean sslEnable) {
        this.sslEnable = sslEnable;
    }

    public String getSslCertType() {
        return sslCertType;
    }

    public void setSslCertType(String sslCertType) {
        this.sslCertType = sslCertType;
    }

    public int getSslPort() {
        return sslPort;
    }

    public void setSslPort(int sslPort) {
        this.sslPort = sslPort;
    }

    public int getSslReadBufferSize() {
        return sslReadBufferSize;
    }

    public void setSslReadBufferSize(int sslReadBufferSize) {
        this.sslReadBufferSize = sslReadBufferSize;
    }

    public int getHttpIdleTimeout() {
        return httpIdleTimeout;
    }

    public void setHttpIdleTimeout(int httpIdleTimeout) {
        this.httpIdleTimeout = httpIdleTimeout;
    }

    public List<Plugin<Request>> getPlugins() {
        return plugins;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isVirtualThreadEnable() {
        return virtualThreadEnable;
    }

    public void setVirtualThreadEnable(boolean virtualThreadEnable) {
        this.virtualThreadEnable = virtualThreadEnable;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
