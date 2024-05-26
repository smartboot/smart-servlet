/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.handler;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import java.io.IOException;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/19
 */
public class ServletRequestListenerHandler extends Handler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletRequestListenerHandler.class);

    @Override
    public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
        ServletContext servletContext = handlerContext.getServletContext();
        List<ServletRequestListener> servletRequestListeners = handlerContext.getServletContext().getDeploymentInfo().getServletRequestListeners();
        ServletRequestEvent servletRequestEvent = servletRequestListeners.isEmpty() ? null : new ServletRequestEvent(servletContext, handlerContext.getRequest());
        servletRequestListeners.forEach(requestListener -> {
            requestListener.requestInitialized(servletRequestEvent);
            LOGGER.info("requestInitialized " + requestListener);
        });
        try {
            doNext(handlerContext);
        } finally {
            servletRequestListeners.forEach(requestListener -> {
                requestListener.requestDestroyed(servletRequestEvent);
                LOGGER.info("requestDestroyed " + requestListener);
            });
        }
    }
}
