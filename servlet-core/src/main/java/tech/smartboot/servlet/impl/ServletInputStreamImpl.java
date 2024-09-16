/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.impl;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.smartboot.http.common.io.BodyInputStream;

import java.io.IOException;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2020/12/12
 */
public class ServletInputStreamImpl extends ServletInputStream {
    private final BodyInputStream inputStream;
    private ReadListener readListener;
    private final HttpServletRequestImpl request;

    public ServletInputStreamImpl(HttpServletRequestImpl request, BodyInputStream inputStream) {
        this.request = request;
        this.inputStream = inputStream;
    }

    @Override
    public boolean isFinished() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReady() {
        if (request.isAsyncStarted()) {
            return inputStream.isReady();
        } else {
            return false;
        }
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        if (readListener == null) {
            throw new NullPointerException();
        }
        if (this.readListener != null) {
            throw new IllegalStateException();
        }
        if (!request.isAsyncStarted()) {
            throw new IllegalStateException();
        }
        inputStream.setReadListener(new org.smartboot.http.common.io.ReadListener() {
            @Override
            public void onDataAvailable() throws IOException {
                readListener.onDataAvailable();
            }

            @Override
            public void onAllDataRead() throws IOException {
                readListener.onAllDataRead();
            }

            @Override
            public void onError(Throwable t) {
                readListener.onError(t);
            }
        });
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

}
