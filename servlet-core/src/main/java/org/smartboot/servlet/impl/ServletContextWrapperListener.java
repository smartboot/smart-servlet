/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.impl;

import javax.servlet.ServletContextListener;

public class ServletContextWrapperListener {
    private final ServletContextListener listener;
    private final boolean dynamic;

    public ServletContextWrapperListener(ServletContextListener listener, boolean dynamic) {
        this.listener = listener;
        this.dynamic = dynamic;
    }

    public ServletContextListener getListener() {
        return listener;
    }

    public boolean isDynamic() {
        return dynamic;
    }
}
