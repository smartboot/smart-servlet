/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.demo.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@RestController
@SpringBootApplication
public class Bootstrap {

    @RequestMapping("/index")
    String home(HttpServletRequest request) throws ServletException {
        Principal principal = request.getUserPrincipal();
        System.out.println(principal);
        return "Hello World!";
    }

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }
}
