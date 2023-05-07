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
