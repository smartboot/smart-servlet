/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SessionProviderImpl.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.session;

import org.smartboot.servlet.provider.SessionProvider;
import org.smartboot.servlet.session.SessionManager;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/28
 */
class SessionProviderImpl implements SessionProvider {
    private final MemorySessionManager sessionManager = new MemorySessionManager();

    @Override
    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
