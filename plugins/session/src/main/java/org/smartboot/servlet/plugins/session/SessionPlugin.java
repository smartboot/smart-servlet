/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SessionPlugin.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.session;

import org.smartboot.servlet.ApplicationRuntime;
import org.smartboot.servlet.plugins.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public class SessionPlugin extends Plugin {

    private final List<SessionProviderImpl> providerList = new ArrayList<>();

    @Override
    public void initPlugin() {
        //Session定期清理
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r, "SessionMonitor");
            thread.setDaemon(true);
            return thread;
        });
        executorService.scheduleWithFixedDelay(() -> providerList.forEach(SessionProviderImpl::clearExpireSession)
                , 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void willStartContainer(ApplicationRuntime containerRuntime) {
        SessionProviderImpl sessionProvider = new SessionProviderImpl();
        sessionProvider.setMaxInactiveInterval(containerRuntime.getDeploymentInfo().getSessionTimeout());
        containerRuntime.setSessionProvider(sessionProvider);
        providerList.add(sessionProvider);
    }

}
