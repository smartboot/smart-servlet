/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: RequestDispatcherImpl.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class RequestDispatcherImpl implements RequestDispatcher {
    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }
}
