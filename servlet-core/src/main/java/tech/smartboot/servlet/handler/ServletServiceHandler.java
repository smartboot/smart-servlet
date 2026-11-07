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

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tech.smartboot.feat.core.common.HttpStatus;

import java.io.IOException;

/**
 * 匹配并执行符合当前请求的Servlet
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletServiceHandler extends Handler {

    @Override
    public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
        ServletRequest request = handlerContext.getRequest();
        ServletResponse response = handlerContext.getResponse();

        //成功匹配到Servlet,直接执行
        try {
            handlerContext.getServletInfo().getServlet().service(request, response);
        } catch (UnavailableException e) {
            //UnavailableException 表示 servlet 暂时或永久不能处理请求。
            //如果 UnavailableException 表示的是一个永久性的不可用，Servlet 容器必须从服务中移除这个 Servlet，调用
            //它的 destroy 方法，并释放 Servlet 实例。所有由于这种原因被容器拒绝的请求，都会返回一个 SC_NOT_FOUND (404) 响应。
            ((HttpServletResponse) response).setStatus(HttpStatus.NOT_FOUND.value());
        } catch (Throwable e) {
            ((HttpServletResponse) response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            Throwable throwable = e;
            String location = handlerContext.getServletContext().getDeploymentInfo().getErrorPageLocation(throwable);
            while (location == null && throwable.getCause() != null) {
                location = handlerContext.getServletContext().getDeploymentInfo().getErrorPageLocation(throwable.getCause());
                throwable = throwable.getCause();
            }

            if (location == null) {
                location = handlerContext.getServletContext().getDeploymentInfo().getErrorPageLocation(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            if (location != null) {
                handlerContext.getServletContext().getRuntime().getDispatcherProvider().error(handlerContext.getServletContext(), location, (HttpServletRequest) request, (HttpServletResponse) response, throwable, handlerContext.getServletInfo().getServletName(), throwable.getMessage());
            } else {
                throw e;
            }
        }
    }
}
