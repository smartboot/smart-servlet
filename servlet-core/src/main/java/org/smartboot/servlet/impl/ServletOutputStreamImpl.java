/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletOutputStreamImpl.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/19
 */
public class ServletOutputStreamImpl extends ServletOutputStream {
    private static final ThreadLocal<byte[]> FIRST_BUFFER = ThreadLocal.withInitial(() -> new byte[1024]);
    private final OutputStream outputStream;
    private boolean committed = false;
    /**
     * buffer仅用于提供response.resetBuffer能力,commit之后即失效
     */
    private byte[] buffer;
    private int count;

    public ServletOutputStreamImpl(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.buffer = FIRST_BUFFER.get();
    }

    @Override
    public boolean isReady() {
        throw new UnsupportedOperationException();
//        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(int b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (committed) {
            outputStream.write(b, off, len);
            return;
        }
        //继续缓存数据
        if (len < buffer.length - count - 1) {
            System.arraycopy(b, off, buffer, count, len);
            count += len;
            return;
        }
        committed = true;
        //buffer中存在缓存数据，先输出
        if (count > 0) {
            outputStream.write(buffer, 0, count);
            count = 0;
        }
        buffer = null;
        outputStream.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    @Override
    public void flush() throws IOException {
        if (count > 0) {
            outputStream.write(buffer, 0, count);
            buffer = null;
        }
        committed = true;
        outputStream.flush();
    }

    public boolean isCommitted() {
        return committed;
    }

    public void resetBuffer() {
        count = 0;
    }

    public void updateBufferSize(int bufferSize) {
        if (committed || count > 0) {
            return;
        }
        buffer = bufferSize > 0 ? new byte[bufferSize] : null;
    }

    public int getCount() {
        return count;
    }

    public int getBufferSize() {
        return buffer == null ? 0 : buffer.length;
    }
}
