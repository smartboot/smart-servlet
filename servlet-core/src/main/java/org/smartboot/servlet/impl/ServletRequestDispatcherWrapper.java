/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SmartServletRequestWrapper.java
 * Date: 2020-11-20
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/20
 */
public class ServletRequestDispatcherWrapper extends HttpServletRequestWrapper {
    private final HttpServletRequestImpl request;

    private final DispatcherType dispatcherType;

    public ServletRequestDispatcherWrapper(HttpServletRequestImpl request, DispatcherType dispatcherType) {
        super(request);
        this.request = request;
        this.dispatcherType = dispatcherType;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return dispatcherType;
    }

    @Override
    public HttpServletRequestImpl getRequest() {
        return request;
    }

}
