/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.smartboot.demo.starter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    @RequestMapping("/plaintext")
    String test() throws ServletException {
        return "Hello World!";
    }

    @RequestMapping("/fileupload")
    String fileupload(HttpServletRequest request) throws ServletException, IOException {
        request.getParts().forEach(part -> System.out.println(part.getSubmittedFileName()));
        return "aa";
    }

    @RequestMapping("/test")
    String test(HttpServletRequest request) throws ServletException, IOException {
        request.getParameterMap().forEach((k, v) -> System.out.println(k + ":" + v[0]));
        return "aa";
    }


    public static String toHtml(String content) {
        return content.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>").replace("\r", "<br/>").replace(" ", "&nbsp;");
    }

    public static void loadFile(File file, StringBuilder sb) throws IOException {
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                loadFile(f, sb);
            }
            if (f.isFile() && f.getName().endsWith(".mdx")) {
                sb.append("## ").append(f.getName()).append("\n");
                try (FileInputStream fis = new FileInputStream(f);) {
                    byte[] bytes = new byte[1024];
                    int len;
                    while ((len = fis.read(bytes)) != -1) {
                        sb.append(new String(bytes, 0, len));
                    }
                }
                sb.append("\n");
            }
        }
    }

    public static void loadSource(File file, StringBuilder sb) throws IOException {
        for (File f : file.listFiles()) {
//            if (f.isDirectory()) {
//                loadFile(f, sb);
//            }
            if (f.isFile() && f.getName().endsWith(".java")) {
                sb.append("## " + f.getName() + "\n");
                sb.append("```java\n");
                try (FileInputStream fis = new FileInputStream(f);) {
                    byte[] bytes = new byte[1024];
                    int len;
                    while ((len = fis.read(bytes)) != -1) {
                        sb.append(new String(bytes, 0, len));
                    }
                }
                sb.append("\n```\n");
            }
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }
}
