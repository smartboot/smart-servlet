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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tech.smartboot.feat.ai.FeatAI;
import tech.smartboot.feat.ai.ModelMeta;
import tech.smartboot.feat.ai.chat.ChatModel;
import tech.smartboot.feat.ai.chat.entity.ResponseMessage;
import tech.smartboot.feat.ai.chat.entity.StreamResponseCallback;

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

    @GetMapping(path = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@RequestParam("content") String content) throws IOException {
        File file = new File("pages/src/content/docs/guides");
        StringBuilder docs = new StringBuilder();
        loadFile(file, docs);

        StringBuilder sourceBuilder = new StringBuilder();
        loadSource(new File("servlet-core/src/main/java/tech/smartboot/servlet/"), sourceBuilder);

        ChatModel chatModel = FeatAI.chatModel(opts -> {
            opts
                                        .model(ModelMeta.GITEE_AI_DeepSeek_R1_Distill_Qwen_32B)
//                    .baseUrl("http://192.168.16.221:11434/v1")
//                    .model("deepseek-r1:32b")
                    .system("你主要负责为这个项目编写使用文档，根据用户要求编写相关章节内容。"
                                    + "参考内容为：\n" + docs
//                            + "\n 实现源码为：\n" + sourceBuilder
                    )
                    .debug(true)
            ;
        });

        SseEmitter sseEmitter = new SseEmitter();
        chatModel.chatStream(content, new StreamResponseCallback() {

            @Override
            public void onCompletion(ResponseMessage responseMessage) {
                try {
                    if (responseMessage.isSuccess()) {
                        sseEmitter.send("<br/>完毕...<br/>");
                    } else {
                        sseEmitter.send(toHtml(responseMessage.getError()));
                        sseEmitter.send("<br/>异常中断...<br/>");
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                sseEmitter.complete();
            }

            @Override
            public void onStreamResponse(String content) {
                System.out.print(content);
                try {
                    sseEmitter.send(toHtml(content));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return sseEmitter;
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
