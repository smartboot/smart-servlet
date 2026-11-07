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

package tech.smartboot.servlet.plugins.dispatcher;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponseWrapper;
import tech.smartboot.servlet.impl.HttpServletResponseImpl;

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
