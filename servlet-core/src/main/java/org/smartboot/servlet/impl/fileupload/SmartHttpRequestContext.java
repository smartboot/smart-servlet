/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.impl.fileupload;

import org.smartboot.servlet.third.commons.fileupload.FileUploadBase;
import org.smartboot.servlet.third.commons.fileupload.UploadContext;
import org.smartboot.http.server.HttpRequest;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;

public class SmartHttpRequestContext implements UploadContext {

    private final HttpRequest request;

    public SmartHttpRequestContext(HttpRequest request) {
        this.request = request;
    }

    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    @Deprecated
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public long contentLength() {
        long size;
        try {
            size = Long.parseLong(request.getHeader(FileUploadBase.CONTENT_LENGTH));
        } catch (NumberFormatException e) {
            size = request.getContentLength();
        }
        return size;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public String toString() {
        return format("ContentLength=%s, ContentType=%s",
                this.contentLength(),
                this.getContentType());
    }

}
