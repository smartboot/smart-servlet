/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SessionPlugin.java
 * Date: 2020-11-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.dispatcher;

import org.smartboot.servlet.ContainerRuntime;
import org.smartboot.servlet.plugins.Plugin;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public class DispatcherPlugin extends Plugin {

    @Override
    public void install() {
        checkSate();
    }

    @Override
    public void startContainer(ContainerRuntime containerRuntime) {
        containerRuntime.setDispatcherProvider(new DispatcherProviderImpl());
    }

    @Override
    public void uninstall() {

    }
}
