/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SessionPlugin.java
 * Date: 2020-11-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.session;

import org.smartboot.servlet.ContainerRuntime;
import org.smartboot.servlet.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public class SessionPlugin extends Plugin {

    private final Map<SessionProviderImpl, ContainerRuntime> runtimeMap = new HashMap<>();


    @Override
    public void install() {
        checkSate();
        //Session定期清理
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r, "SessionMonitor");
            thread.setDaemon(true);
            return thread;
        });
        executorService.scheduleWithFixedDelay(() -> runtimeMap.forEach((sessionManager, runtime) -> {
                    System.out.println("clear expire session...");
                    sessionManager.clearExpireSession();
                })
                , 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void startContainer(ContainerRuntime containerRuntime) {
        SessionProviderImpl sessionManager = new SessionProviderImpl();
        runtimeMap.put(sessionManager, containerRuntime);
        containerRuntime.getDeploymentInfo().setSessionProvider(sessionManager);
    }

    @Override
    public void uninstall() {

    }
}
