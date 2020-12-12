/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletInputStreamImpl.java
 * Date: 2020-12-12
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2020/12/12
 */
public class ServletInputStreamImpl extends ServletInputStream {
    private final InputStream inputStream;

    public ServletInputStreamImpl(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public boolean isFinished() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReady() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
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
