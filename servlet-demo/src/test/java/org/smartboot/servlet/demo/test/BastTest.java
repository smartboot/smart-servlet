/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: BastTest.java
 * Date: 2021-05-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */
package org.smartboot.servlet.demo.test;

import org.smartboot.http.client.HttpClient;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/14
 */
public class BastTest {

    protected HttpClient getSmartClient() {
        HttpClient httpClient = new HttpClient("127.0.0.1", 8081);
        httpClient.connect();
        return httpClient;
    }

    protected HttpClient getTomcatClient() {
        HttpClient httpClient = new HttpClient("127.0.0.1", 8082);
        httpClient.connect();
        return httpClient;
    }
}
