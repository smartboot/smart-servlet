/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: DemoServlet.java
 * Date: 2021-05-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */
package org.smartboot.servlet.demo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/14
 */
public class DemoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getOutputStream().write(("Hello World").getBytes(StandardCharsets.UTF_8));
    }
}
