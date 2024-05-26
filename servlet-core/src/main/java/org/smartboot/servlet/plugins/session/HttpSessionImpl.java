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

import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.util.CollectionUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/19
 */
class HttpSessionImpl implements HttpSession {

    private final long creationTime = System.currentTimeMillis();
    private final Map<String, Object> attributes = new HashMap<>();
    private String sessionId;
    private final ServletContextImpl servletContext;
    private final HttpSessionContext httpSessionContext;
    private volatile long lastAccessed;
    private volatile int maxInactiveInterval;
    private volatile boolean invalid;

    private String authType;
    private Principal principal = null;

    public HttpSessionImpl(HttpSessionContext httpSessionContext, String sessionId, ServletContextImpl servletContext) {
        this.httpSessionContext = httpSessionContext;
        this.sessionId = sessionId;
        this.servletContext = servletContext;

        List<HttpSessionListener> sessionListeners = servletContext.getDeploymentInfo().getHttpSessionListeners();
        HttpSessionEvent httpSessionEvent = sessionListeners.isEmpty() ? null : new HttpSessionEvent(this);
        sessionListeners.forEach(httpSessionListener -> httpSessionListener.sessionCreated(httpSessionEvent));
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public long getLastAccessedTime() {
        checkState();
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return httpSessionContext;
    }

    @Override
    public Object getAttribute(String name) {
        checkState();
        return attributes.get(name);
    }

    @Override
    public Object getValue(String name) {
        checkState();
        return getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkState();
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String[] getValueNames() {
        checkState();
        return attributes.keySet().toArray(new String[0]);
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkState();
        Object replace = attributes.put(name, value);
        if (CollectionUtils.isNotEmpty(servletContext.getDeploymentInfo().getSessionAttributeListeners())) {
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
            if (replace == null) {
                servletContext.getDeploymentInfo().getSessionAttributeListeners().forEach(request -> request.attributeAdded(event));
            } else {
                servletContext.getDeploymentInfo().getSessionAttributeListeners().forEach(request -> request.attributeReplaced(event));
            }
        }
    }

    @Override
    public void putValue(String name, Object value) {
        checkState();
        setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        checkState();
        Object o = attributes.remove(name);
        if (CollectionUtils.isNotEmpty(servletContext.getDeploymentInfo().getSessionAttributeListeners())) {
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, o);
            servletContext.getDeploymentInfo().getSessionAttributeListeners().forEach(request -> request.attributeRemoved(event));
        }
    }

    @Override
    public void removeValue(String name) {
        checkState();
        removeAttribute(name);
    }

    @Override
    public void invalidate() {
        checkState();
        invalid();
    }

    public void invalid() {
        List<HttpSessionListener> sessionListeners = servletContext.getDeploymentInfo().getHttpSessionListeners();
        HttpSessionEvent httpSessionEvent = sessionListeners.isEmpty() ? null : new HttpSessionEvent(this);
        sessionListeners.forEach(httpSessionListener -> httpSessionListener.sessionDestroyed(httpSessionEvent));
        invalid = true;
    }

    public void changeSessionId(String sessionId) {
        String oldSessionId = this.sessionId;
        this.sessionId = sessionId;
        List<HttpSessionIdListener> sessionListeners = servletContext.getDeploymentInfo().getHttpSessionIdListeners();
        HttpSessionEvent httpSessionEvent = sessionListeners.isEmpty() ? null : new HttpSessionEvent(this);
        sessionListeners.forEach(httpSessionListener -> httpSessionListener.sessionIdChanged(httpSessionEvent, oldSessionId));
    }

    @Override
    public boolean isNew() {
        checkState();
        return false;
    }

    private void checkState() {
        if (invalid) {
            throw new IllegalStateException();
        }
    }
}
