/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletTest.java
 * Date: 2021-05-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.testsuite.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.client.HttpClient;

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

    /**
     * 精确匹配
     */
    @Test
    public void test1() {
        checkPath("/demo", smartClient, tomcatClient);
    }

    /**
     * 前缀匹配
     */
    @Test
    public void test2() {
        checkPath("/pathMatch", smartClient, tomcatClient);
    }

    /**
     * 前缀匹配
     */
    @Test
    public void test3() {
        checkPath("/pathMatch/1", smartClient, tomcatClient);
    }

    /**
     * 前缀匹配，包含query
     */
    @Test
    public void test4() {
        checkPath("/pathMatch/1?abc=c&bdc=4", smartClient, tomcatClient);
    }

    @After
    public void destroy() {
        smartClient.close();
        tomcatClient.close();
    }
}
