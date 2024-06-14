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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Date;

@RestController
@SpringBootApplication
public class Bootstrap {
    @GetMapping(path = "/add")
    void add(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        session.setAttribute("time", new Date().getTime());
        System.out.println("session...");
    }
    @GetMapping(path = "/logout")
    void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        session.invalidate();
        session = request.getSession();
        session.invalidate();
        session = request.getSession();
        session.invalidate();
        request.getSession();
        System.out.println("invalidate...");
    }

    @RequestMapping("/index")
    String home(HttpServletRequest request, @SessionAttribute(name = "time", required = false) String time) throws ServletException {
        Principal principal = request.getUserPrincipal();
        System.out.println(principal);
        System.out.println(request.getSession().getAttribute("time"));
        return "Hello World!" + time;
    }

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }
}
