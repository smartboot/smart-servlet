/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: Plugin.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.servlet.ApplicationRuntime;
import org.smartboot.servlet.ContainerRuntime;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public abstract class Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(Plugin.class);
    /**
     * 是否已安装
     */
    private boolean installed;
    /**
     * 插件名称
     */
    private String pluginName;

    /**
     * 获取插件名称
     *
     * @return
     */
    public String pluginName() {
        if (pluginName == null) {
            pluginName = this.getClass().getSimpleName();
        }
        return pluginName;
    }


    /**
     * 安装插件,需要在servlet服务启动前调用
     */
    public final void install(ContainerRuntime containerRuntime) {
        checkSate();
        initPlugin(containerRuntime);
        installed = true;
    }

    /**
     * 初始化插件
     */
    protected void initPlugin(ContainerRuntime containerRuntime) {
        LOGGER.info("plugin:[" + pluginName() + "] do nothing when initPlugin!");
    }

    /**
     * servlet容器被启动成功之后被调用
     *
     * @param containerRuntime 当前启动成功的子容器
     */
    public void onContainerStartSuccess(ApplicationRuntime containerRuntime) {
        LOGGER.info("plugin:[" + pluginName() + "] do nothing for container: " + containerRuntime.getContextPath() + " when start success!");
    }


    /**
     * servlet子容器启动前被调用
     *
     * @param containerRuntime 当前即将被启动的子容器
     */
    public void willStartContainer(ApplicationRuntime containerRuntime) {
        LOGGER.info("plugin:[" + pluginName() + "] do nothing for container: " + containerRuntime.getContextPath() + " before start!");
    }

    /**
     * servlet子容器启动失败时被调用
     *
     * @param containerRuntime 当前启动失败的子容器
     */
    public void whenContainerStartError(ApplicationRuntime containerRuntime, Throwable throwable) {
        LOGGER.info("plugin:[" + pluginName() + "] do nothing for container: " + containerRuntime.getContextPath() + " when start error!");
    }

    /**
     * 即将消耗子容器
     *
     * @param containerRuntime 即将被消耗的子容器
     */
    public void willStopContainer(ApplicationRuntime containerRuntime) {
        LOGGER.info("plugin:[" + pluginName() + "]do nothing for container: " + containerRuntime.getContextPath() + " before stop!");
    }

    /**
     * 子容器已销毁
     *
     * @param containerRuntime 当前被消耗的子容器
     */
    public void onContainerStopped(ApplicationRuntime containerRuntime) {
        LOGGER.info("plugin:[" + pluginName() + "] do nothing for container: " + containerRuntime.getContextPath() + " when stop!");
    }

    /**
     * 卸载插件,在容器服务停止前调用
     */
    public final void uninstall() {
        destroyPlugin();
    }

    /**
     * 销毁插件
     */
    protected void destroyPlugin() {
        LOGGER.info("plugin:[" + pluginName() + "] do nothing when destroyPlugin!");
    }

    private void checkSate() {
        if (installed) {
            throw new IllegalStateException("plugin [ " + pluginName() + " ] has installed!");
        }
    }
}
