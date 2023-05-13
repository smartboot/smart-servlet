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
import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.provider.SessionProvider;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class SessionCookieConfigImpl implements SessionCookieConfig {
    private String name = SessionProvider.DEFAULT_SESSION_COOKIE_NAME;
    private String path;
    private String domain;
    private int maxAge = -1;
    private boolean secure;
    private boolean httpOnly;
    private String comment;
    private final ServletContextRuntime servletContextRuntime;

    public SessionCookieConfigImpl(ServletContextRuntime servletContextRuntime) {
        this.servletContextRuntime = servletContextRuntime;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        check();
        this.name = name;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public void setDomain(String domain) {
        check();
        this.domain = domain;
    }

    @Override
    public String getPath() {
        return path;
    }

    private void check() {
        if (servletContextRuntime.isStarted()) {
            throw new IllegalStateException("sessionCookie can not be set after the web application has started");
        }
    }

    @Override
    public void setPath(String path) {
        check();
        this.path = path;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        check();
        this.comment = comment;
    }

    @Override
    public boolean isHttpOnly() {
        return httpOnly;
    }

    @Override
    public void setHttpOnly(boolean httpOnly) {
        check();
        this.httpOnly = httpOnly;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public void setSecure(boolean secure) {
        check();
        this.secure = secure;
    }

    @Override
    public int getMaxAge() {
        return maxAge;
    }

    @Override
    public void setMaxAge(int maxAge) {
        check();
        this.maxAge = maxAge;
    }
}
