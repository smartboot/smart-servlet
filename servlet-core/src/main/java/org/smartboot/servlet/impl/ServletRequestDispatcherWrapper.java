/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SmartServletRequestWrapper.java
 * Date: 2020-11-20
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import javax.servlet.ServletRequestWrapper;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/20
 */
public class ServletRequestDispatcherWrapper extends ServletRequestWrapper {
    private final HttpServletRequestImpl request;

    public ServletRequestDispatcherWrapper(HttpServletRequestImpl request) {
        super(request);
        this.request = request;
    }

    @Override
    public HttpServletRequestImpl getRequest() {
        return request;
    }

}
