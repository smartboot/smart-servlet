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
import tech.smartboot.feat.core.common.io.BodyInputStream;

import java.io.IOException;

public class UpgradeServletInputStream extends ServletInputStream {
    private final BodyInputStream inputStream;

    public UpgradeServletInputStream(BodyInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public boolean isFinished() {
        return inputStream.isFinished();
    }

    @Override
    public boolean isReady() {
        return inputStream.isReady();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        inputStream.setReadListener(new tech.smartboot.feat.core.common.io.ReadListener() {
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
