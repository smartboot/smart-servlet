/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SessionCookieConfigImpl.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import javax.servlet.SessionCookieConfig;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class SessionCookieConfigImpl implements SessionCookieConfig {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String getDomain() {
        return null;
    }

    @Override
    public void setDomain(String domain) {

    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public void setPath(String path) {

    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public void setComment(String comment) {

    }

    @Override
    public boolean isHttpOnly() {
        return false;
    }

    @Override
    public void setHttpOnly(boolean httpOnly) {

    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public void setSecure(boolean secure) {

    }

    @Override
    public int getMaxAge() {
        return 0;
    }

    @Override
    public void setMaxAge(int maxAge) {

    }
}
