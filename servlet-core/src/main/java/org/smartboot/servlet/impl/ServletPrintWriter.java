/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletPrintWriter.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/10
 */
public class ServletPrintWriter extends Writer {
    private final OutputStream outputStream;
    private final String charset;
    public ServletPrintWriter(OutputStream outputStream, String charset) {
        super(outputStream);
        this.outputStream = outputStream;
        this.charset = charset;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        byte[] bytes = new String(cbuf, off, len).getBytes(charset);
        outputStream.write(bytes);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
