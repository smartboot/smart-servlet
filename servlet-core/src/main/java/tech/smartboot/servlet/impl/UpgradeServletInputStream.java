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
