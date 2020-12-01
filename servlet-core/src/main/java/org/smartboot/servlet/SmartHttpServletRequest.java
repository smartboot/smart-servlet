/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SmartHttpServletRequest.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/22
 */
public interface SmartHttpServletRequest extends HttpServletRequest {
    void setRequestUri(String requestUri);

    void setServletPath(String servletPath);

    void setPathInfo(String pathInfo);
}
