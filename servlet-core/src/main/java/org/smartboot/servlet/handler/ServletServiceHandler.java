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

import javax.servlet.Servlet;

/**
 * 匹配并执行符合当前请求的Servlet
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletServiceHandler extends Handler {

    @Override
    public void handleRequest(HandlerContext handlerContext) throws Exception {
        Servlet servlet = handlerContext.getServlet();
        if (servlet == null) {
            throw new HttpException(HttpStatus.NOT_FOUND);
        }
        servlet.service(handlerContext.getRequest(), handlerContext.getResponse());
        doNext(handlerContext);
    }
}
