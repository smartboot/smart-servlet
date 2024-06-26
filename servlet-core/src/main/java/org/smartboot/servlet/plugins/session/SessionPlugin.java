/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.plugins.session;

import org.smartboot.servlet.Container;
import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.plugins.Plugin;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public class SessionPlugin extends Plugin {

    @Override
    public void initPlugin(Container container) {

    }

    @Override
    public void willStartContainer(ServletContextRuntime containerRuntime) {
        SessionProviderImpl sessionProvider = new SessionProviderImpl();
        sessionProvider.setMaxInactiveInterval(containerRuntime.getDeploymentInfo().getSessionTimeout());
        containerRuntime.setSessionProvider(sessionProvider);
    }

}
