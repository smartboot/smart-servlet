/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: Plugin.java
 * Date: 2020-11-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins;

import org.smartboot.servlet.ContainerRuntime;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public abstract class Plugin {

    /**
     * 是否已安装
     */
    protected boolean installed;
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


    public abstract void install();

    public abstract void startContainer(ContainerRuntime containerRuntime);

    public abstract void uninstall();

    protected void checkSate() {
        if (installed) {
            throw new IllegalStateException("plugin [ " + pluginName + " ] has installed!");
        }
    }
}
