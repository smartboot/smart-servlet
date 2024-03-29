/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
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
