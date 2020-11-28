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
    private final OutputStream outputStream;
    private boolean committed = false;
    private byte[] buffer;
    private int count;

    public ServletOutputStreamImpl(OutputStream outputStream) {
        this.outputStream = outputStream;
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
    public void write(int b) throws IOException {
        committed = true;
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (buffer == null) {
            committed = true;
            outputStream.write(b, off, len);
            return;
        }
        if (len < buffer.length - count - 1) {
            System.arraycopy(b, off, buffer, count, len);
            count += len;
            return;
        }
        committed = true;
        if (count > 0) {
            outputStream.write(buffer, 0, count);
            count = 0;
        }
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
        buffer = bufferSize > 0 ? null : new byte[bufferSize];
    }

    public int getCount() {
        return count;
    }

    public int getBufferSize() {
        return buffer == null ? 0 : buffer.length;
    }
}
