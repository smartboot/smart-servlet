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

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.util.CollectionUtils;
import org.smartboot.socket.timer.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/19
 */
class HttpSessionImpl implements HttpSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpSessionImpl.class);
    private final long creationTime = System.currentTimeMillis();
    private final Map<String, Object> attributes = new HashMap<>();
    private String sessionId;
    private final ServletContextImpl servletContext;
    private final SessionProviderImpl sessionProvider;
    private volatile long lastAccessed = creationTime;
    private volatile int maxInactiveInterval;
    private volatile boolean invalid;
    private TimerTask timerTask;
    private HttpServletResponse response;

    public HttpSessionImpl(SessionProviderImpl sessionProvider, String sessionId, ServletContextImpl servletContext) {
        this.sessionProvider = sessionProvider;
        this.sessionId = sessionId;
        this.servletContext = servletContext;

        List<HttpSessionListener> sessionListeners = servletContext.getDeploymentInfo().getHttpSessionListeners();
        HttpSessionEvent httpSessionEvent = sessionListeners.isEmpty() ? null : new HttpSessionEvent(this);
        sessionListeners.forEach(httpSessionListener -> httpSessionListener.sessionCreated(httpSessionEvent));
    }

    @Override
    public long getCreationTime() {
        checkState();
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
        updateTimeoutTask();
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
        updateTimeoutTask();
    }

    private synchronized void updateTimeoutTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (maxInactiveInterval <= 0) {
            return;
        }
        timerTask = sessionProvider.getTimer().schedule(() -> {
            LOGGER.info("sessionId:{} will be expired, lastAccessedTime:{} ,maxInactiveInterval:{}", sessionId, lastAccessed, maxInactiveInterval);
            invalid();
        }, maxInactiveInterval, TimeUnit.SECONDS);
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return sessionProvider;
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
            if (replace == null) {
                HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
                servletContext.getDeploymentInfo().getSessionAttributeListeners().forEach(request -> request.attributeAdded(event));
            } else {
                HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, replace);
                servletContext.getDeploymentInfo().getSessionAttributeListeners().forEach(request -> request.attributeReplaced(event));
            }
        }

        //After this method executes, and if the new object implements HttpSessionBindingListener, the container calls HttpSessionBindingListener.valueBound.
        // The container then notifies any HttpSessionAttributeListeners in the web application.
        //If an object was already bound to this session of this name that implements HttpSessionBindingListener, its HttpSessionBindingListener. valueUnbound method is called.
        if (replace != value && replace instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) replace).valueUnbound(new HttpSessionBindingEvent(this, name, replace));
        }
        if (value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name, value));
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
        if (o instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) o).valueUnbound(new HttpSessionBindingEvent(this, name, o));
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
        Cookie cookie = new Cookie(servletContext.getSessionCookieConfig().getName(), sessionId);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public boolean isInvalid() {
        return invalid;
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
        return creationTime == lastAccessed;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    private void checkState() {
        if (invalid) {
            throw new IllegalStateException();
        }
    }
}
