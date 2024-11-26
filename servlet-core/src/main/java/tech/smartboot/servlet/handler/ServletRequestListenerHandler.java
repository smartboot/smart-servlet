/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.handler;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;

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
        if (handlerContext.getRequest().getDispatcherType() != DispatcherType.REQUEST) {
            doNext(handlerContext);
            return;
        }
        ServletContext servletContext = handlerContext.getServletContext();
        List<ServletRequestListener> servletRequestListeners = handlerContext.getServletContext().getDeploymentInfo().getServletRequestListeners();
        if (servletRequestListeners.isEmpty()) {
            doNext(handlerContext);
            return;
        }
        ServletRequestEvent servletRequestEvent = new ServletRequestEvent(servletContext, handlerContext.getRequest());
        servletRequestListeners.forEach(requestListener -> {
            requestListener.requestInitialized(servletRequestEvent);
            LOGGER.info("requestInitialized " + requestListener);
        });
        try {
            doNext(handlerContext);
        } finally {
            for (int i = servletRequestListeners.size() - 1; i >= 0; i--) {
                servletRequestListeners.get(i).requestDestroyed(servletRequestEvent);
            }
        }
    }
}
