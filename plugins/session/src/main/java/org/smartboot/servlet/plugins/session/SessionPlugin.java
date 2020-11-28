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

    private final List<ContainerRuntime> runtimes = new ArrayList<>();


    @Override
    public void install() {
        checkSate();
        //Session定期清理
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r, "SessionMonitor");
            thread.setDaemon(true);
            return thread;
        });
        executorService.scheduleWithFixedDelay(() -> runtimes.forEach(runtimes -> {
                    System.out.println("回收过期Session");
                    runtimes.getDeploymentInfo().getSessionManager().clearExpireSession();
                })
                , 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void startContainer(ContainerRuntime containerRuntime) {
        MemorySessionManager sessionManager = new MemorySessionManager();
        containerRuntime.getDeploymentInfo().setSessionProvider(() -> sessionManager);
        runtimes.add(containerRuntime);
    }

    @Override
    public void uninstall() {

    }
}
