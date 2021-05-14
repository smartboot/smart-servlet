/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletTest.java
 * Date: 2021-05-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.demo.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.client.HttpClient;
import org.smartboot.http.client.HttpResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/14
 */
public class ServletTest extends BastTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletTest.class);
    private HttpClient smartClient;
    private HttpClient tomcatClient;

    @Before
    public void init() {
        smartClient = getSmartClient();
        tomcatClient = getTomcatClient();
    }

    @Test
    public void testHelloWorld() throws ExecutionException, InterruptedException {
        Future<HttpResponse> smartFuture = smartClient.get("/demo").onSuccess(resp -> {
            LOGGER.info("smart-servlet response: {}", resp.body());
        }).send();
        Future<HttpResponse> tomcatFuture = tomcatClient.get("/demo").onSuccess(resp -> {
            LOGGER.info("tomcat response: {}", resp.body());
        }).send();
        Assert.assertEquals("body 响应不同", smartFuture.get().body(), tomcatFuture.get().body());
    }

    @After
    public void destroy() {
        smartClient.close();
        tomcatClient.close();
    }
}
