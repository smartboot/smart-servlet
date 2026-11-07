/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tech.smartboot.servlet.handler;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import tech.smartboot.feat.core.common.logging.Logger;
import tech.smartboot.feat.core.common.logging.LoggerFactory;

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
