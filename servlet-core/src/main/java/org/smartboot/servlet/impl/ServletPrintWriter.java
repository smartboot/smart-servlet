/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletPrintWriter.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.servlet.ContainerRuntime;
import org.smartboot.socket.buffer.VirtualBuffer;

import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/10
 */
public class ServletPrintWriter extends Writer {
    private static final int BUFFER_LIMIT = 128;
    private final ServletOutputStreamImpl servletOutputStream;
    private final CharsetEncoder charsetEncoder;
    private final ContainerRuntime containerRuntime;

    public ServletPrintWriter(ServletOutputStreamImpl servletOutputStream, String charset, ContainerRuntime containerRuntime) {
        super(servletOutputStream);
        this.containerRuntime = containerRuntime;
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
        while (buffer.hasRemaining()) {
            VirtualBuffer virtualBuffer = containerRuntime.getMemoryPoolProvider().getBufferPage().allocate(BUFFER_LIMIT);
            charsetEncoder.encode(buffer, virtualBuffer.buffer(), true);
            virtualBuffer.buffer().flip();
            servletOutputStream.write(virtualBuffer);
            if (buffer.hasRemaining()) {
                System.out.println("aaa " + buffer.remaining());
            }
        }
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
