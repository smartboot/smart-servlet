/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletServiceHandler.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.exception.WrappedRuntimeException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * 匹配并执行符合当前请求的Servlet
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletServiceHandler extends Handler {

    @Override
    public void handleRequest(HandlerContext handlerContext) {
        Servlet servlet = handlerContext.getServlet();
        if (servlet == null) {
            throw new HttpException(HttpStatus.NOT_FOUND);
        }
        try {
            servlet.service(handlerContext.getRequest(), handlerContext.getResponse());
        } catch (ServletException | IOException e) {
            throw new WrappedRuntimeException(e);
        }
        doNext(handlerContext);
    }
}
