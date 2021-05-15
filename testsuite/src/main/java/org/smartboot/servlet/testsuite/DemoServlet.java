/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: DemoServlet.java
 * Date: 2021-05-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */
package org.smartboot.servlet.testsuite;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/14
 */
public class DemoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("contextPath", req.getContextPath());
        jsonObject.put("servletPath", req.getServletPath());
        jsonObject.put("pathInfo", req.getPathInfo());
        jsonObject.put("queryString", req.getQueryString());
        Enumeration enumeration = req.getParameterNames();
        while ((enumeration.hasMoreElements())) {
            String param = String.valueOf(enumeration.nextElement());
            jsonObject.put("param_" + param, req.getParameter(param));
        }
        resp.getOutputStream().write((jsonObject.toJSONString()).getBytes(StandardCharsets.UTF_8));
    }
}
