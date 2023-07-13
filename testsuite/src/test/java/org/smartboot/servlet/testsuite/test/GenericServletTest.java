/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.testsuite.test;

import com.sun.ts.tests.servlet.api.jakarta_servlet.genericservlet.URLClient;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class GenericServletTest extends URLClient {
    @Test
    public void init_ServletConfigServletExceptionTest() throws Exception {
        String testName = "init_ServletConfigServletExceptionTest";
        TEST_PROPS.setProperty("testname", testName);
        TEST_PROPS.setProperty("status-code", "500");
        Properties var10000 = TEST_PROPS;
        String var10002 = this.getContextRoot();
        var10000.setProperty("request", "GET " + var10002 + "/" + testName + " HTTP/1.1");
        TEST_PROPS.setProperty("search_string", "Status Code: 500|Exception: javax.servlet.ServletException: in init of Init_ServletConfigServletExceptionTestServlet");
        this.invoke();
    }
}
