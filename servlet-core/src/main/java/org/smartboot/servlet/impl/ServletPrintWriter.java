/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletPrintWriter.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.socket.buffer.BufferPage;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.buffer.VirtualBuffer;

import java.io.IOException;
import java.io.Writer;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/10
 */
public class ServletPrintWriter extends Writer {
    private static final BufferPage bufferPage = new BufferPagePool(10 * 1024 * 1024, 6, true).allocateBufferPage();
    private final ServletOutputStreamImpl servletOutputStream;
    private final CharsetEncoder charsetEncoder;

    public ServletPrintWriter(ServletOutputStreamImpl servletOutputStream, String charset) {
        super(servletOutputStream);
        this.servletOutputStream = servletOutputStream;
        this.charsetEncoder = Charset.forName(charset).newEncoder();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        write(CharBuffer.wrap(cbuf, off, len));
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        write(CharBuffer.wrap(str, off, len));
    }

    private void write(CharBuffer buffer) throws IOException {
        VirtualBuffer virtualBuffer = bufferPage.allocate(buffer.remaining() * 2);
        CoderResult result = charsetEncoder.encode(buffer, virtualBuffer.buffer(), true);
        ((Buffer) (virtualBuffer.buffer())).flip();
        servletOutputStream.write(virtualBuffer);
    }

    @Override
    public void flush() throws IOException {
        servletOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        servletOutputStream.close();
    }
}
