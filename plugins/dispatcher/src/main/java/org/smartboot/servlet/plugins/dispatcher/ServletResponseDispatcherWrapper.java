/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletResponseDispatcherWrapper.java
 * Date: 2020-11-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.dispatcher;

import org.smartboot.servlet.impl.HttpServletResponseImpl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Locale;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/19
 */
class ServletResponseDispatcherWrapper extends HttpServletResponseWrapper {

    private final HttpServletResponseImpl response;
    private boolean included;

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response
     * @throws IllegalArgumentException if the response is null
     */
    public ServletResponseDispatcherWrapper(HttpServletResponseImpl response, boolean included) {
        super(response);
        this.included = included;
        this.response = response;
    }

    @Override
    public void addCookie(Cookie cookie) {
        if (included) {
            return;
        }
        super.addCookie(cookie);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        if (included) {
            return;
        }
        super.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        if (included) {
            return;
        }
        super.sendError(sc);
    }

    @Override
    public void setDateHeader(String name, long date) {
        if (included) {
            return;
        }
        super.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        if (included) {
            return;
        }
        super.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        if (included) {
            return;
        }
        super.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        if (included) {
            return;
        }
        super.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        if (included) {
            return;
        }
        super.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        if (included) {
            return;
        }
        super.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc) {
        if (included) {
            return;
        }
        super.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        if (included) {
            return;
        }
        super.setStatus(sc, sm);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        if (included) {
            return;
        }
        super.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        if (included) {
            return;
        }
        super.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        if (included) {
            return;
        }
        super.setContentLengthLong(len);
    }

    @Override
    public void setContentType(String type) {
        if (included) {
            return;
        }
        super.setContentType(type);
    }

    @Override
    public void setLocale(Locale loc) {
        if (included) {
            return;
        }
        super.setLocale(loc);
    }

    @Override
    public HttpServletResponseImpl getResponse() {
        return response;
    }
}
