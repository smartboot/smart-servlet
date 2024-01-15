package org.smartboot.servlet;

import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.WebSocketHandler;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.impl.WebSocketRequestImpl;
import org.smartboot.http.server.impl.WebSocketResponseImpl;

import java.util.concurrent.CompletableFuture;

/**
 * @author huqiang
 * @since 2024/1/15 20:22
 */
public class BootStrapTest {


    public static void main(String[] args) {
        HttpBootstrap bootstrap = new HttpBootstrap();
                bootstrap.httpHandler(new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, CompletableFuture<Object> completableFuture) {
            }
        }).webSocketHandler(new WebSocketHandler() {
            @Override
            public void whenHeaderComplete(WebSocketRequestImpl request, WebSocketResponseImpl response) {
            }

            @Override
            public void handle(WebSocketRequest request, WebSocketResponse response) throws Throwable {
            }
        });
        bootstrap.configuration().bannerEnabled(false).readBufferSize(1024 * 1024).debug(false);
        bootstrap.setPort(8001);
        bootstrap.start();
    }
}
