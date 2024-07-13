/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.jakarta.third.commons.fileupload;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>Abstracts access to the request information needed for file uploads. This
 * interface should be implemented for each type of request that may be
 * handled by FileUpload, such as servlets and portlets.</p>
 *
 * @since FileUpload 1.1
 */
public interface RequestContext {

    /**
     * Retrieve the character encoding for the request.
     *
     * @return The character encoding for the request.
     */
    String getCharacterEncoding();

    /**
     * Retrieve the content type of the request.
     *
     * @return The content type of the request.
     */
    String getContentType();

    /**
     * Retrieve the content length of the request.
     *
     * @return The content length of the request.
     * @deprecated 1.3 Use {@link UploadContext#contentLength()} instead
     */
    @Deprecated
    int getContentLength();

    /**
     * Retrieve the input stream for the request.
     *
     * @return The input stream for the request.
     *
     * @throws IOException if a problem occurs.
     */
    InputStream getInputStream() throws IOException;

}
