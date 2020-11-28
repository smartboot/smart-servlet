/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SessionProvider.java
 * Date: 2020-11-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.provider;

import org.smartboot.servlet.session.SessionManager;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public interface SessionProvider {
    SessionManager getSessionManager();
}
