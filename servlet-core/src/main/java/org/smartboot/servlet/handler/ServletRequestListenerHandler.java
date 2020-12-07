/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletRequestListenerHandler.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.http.logging.RunLogger;
import org.smartboot.servlet.HandlerContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import java.util.List;
import java.util.logging.Level;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/19
 */
public class ServletRequestListenerHandler extends Handler {

    @Override
    public void handleRequest(HandlerContext handlerContext) {
        ServletContext servletContext = handlerContext.getServletContext();
        List<ServletRequestListener> servletRequestListeners = handlerContext.getServletContext().getDeploymentInfo().getServletRequestListeners();
        ServletRequestEvent servletRequestEvent = servletRequestListeners.isEmpty() ? null : new ServletRequestEvent(servletContext, handlerContext.getRequest());
        if (!servletRequestListeners.isEmpty()) {
            servletRequestListeners.forEach(requestListener -> {
                requestListener.requestInitialized(servletRequestEvent);
                RunLogger.getLogger().log(Level.INFO, "requestInitialized " + requestListener);
            });
        }
        try {
            doNext(handlerContext);
        } finally {
            if (!servletRequestListeners.isEmpty()) {
                servletRequestListeners.forEach(requestListener -> {
                    requestListener.requestDestroyed(servletRequestEvent);
                    RunLogger.getLogger().log(Level.INFO, "requestDestroyed " + requestListener);
                });
            }
        }
    }
}
