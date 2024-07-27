/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.servlet.third.commons.fileupload;

/**
 * Enhanced access to the request information needed for file uploads,
 * which fixes the Content Length data access in {@link RequestContext}.
 *
 * The reason of introducing this new interface is just for backward compatibility
 * and it might vanish for a refactored 2.x version moving the new method into
 * RequestContext again.
 *
 * @since 1.3
 */
public interface UploadContext extends RequestContext {

    /**
     * Retrieve the content length of the request.
     *
     * @return The content length of the request.
     * @since 1.3
     */
    long contentLength();

}
