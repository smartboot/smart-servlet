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

import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/11/19
 */
public class PartImpl implements Part {

    private final tech.smartboot.feat.core.common.multipart.Part fileItem;
    private final File location;

    public PartImpl(tech.smartboot.feat.core.common.multipart.Part fileItem, File location) {
        this.fileItem = fileItem;
        this.location = location;
    }

    @Override
    public void delete() throws IOException {
        fileItem.delete();
    }

    @Override
    public String getContentType() {
        return fileItem.getContentType();
    }

    @Override
    public String getHeader(String name) {
        return fileItem.getHeader(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return fileItem.getHeaderNames();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return fileItem.getHeaders(name);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return fileItem.getInputStream();
    }

    @Override
    public String getName() {
        return fileItem.getName();
    }

    @Override
    public long getSize() {
        return fileItem.getSize();
    }

    @Override
    public void write(String fileName) throws IOException {
        fileItem.write(fileName);
    }

    /*
     * Adapted from FileUploadBase.getFileName()
     */
    @Override
    public String getSubmittedFileName() {
        return fileItem.getSubmittedFileName();
    }
}
