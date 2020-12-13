/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServerConfig.java
 * Date: 2020-12-10
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.starter;

import org.smartboot.http.utils.Param;

/**
 * @author 三刀
 * @version V1.0 , 2020/12/10
 */
public class ServerConfig {
    /**
     * http服务端口号
     */
    @Param(name = "http.port", value = "8080")
    private int port;

    /**
     * 根上下文
     */
    @Param(name = "root.context")
    private String rootContext;

    /**
     * 插件的配置文件,例如：plugin.configs=session.conf,dispatcher.properties
     */
    @Param(name = "plugin.configs")
    private String pluginConfigs;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPluginConfigs() {
        return pluginConfigs;
    }

    public void setPluginConfigs(String pluginConfigs) {
        this.pluginConfigs = pluginConfigs;
    }

    public String getRootContext() {
        return rootContext;
    }

    public void setRootContext(String rootContext) {
        this.rootContext = rootContext;
    }
}
