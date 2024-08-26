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

    private final org.smartboot.http.common.multipart.Part fileItem;
    private final File location;

    public PartImpl(org.smartboot.http.common.multipart.Part fileItem, File location) {
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
