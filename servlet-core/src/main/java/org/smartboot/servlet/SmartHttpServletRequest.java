/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SmartHttpServletRequest.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;

import org.smartboot.servlet.conf.ServletInfo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/22
 */
public interface SmartHttpServletRequest extends HttpServletRequest {
    void setRequestUri(String requestUri);

    /**
     * 设置servletPath索引位置,若为null者传入负数
     *
     * @param start 起始点位
     * @param end   结束点位
     */
    void setServletPath(int start, int end);

    /**
     * 设置pathInfo索引位置,若为null者传入负数
     *
     * @param start 起始点位
     * @param end   结束点位
     */
    void setPathInfo(int start, int end);

    void setServletInfo(ServletInfo servletInfo);
}
