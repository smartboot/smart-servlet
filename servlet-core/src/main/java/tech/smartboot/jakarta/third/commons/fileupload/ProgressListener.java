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

/**
 * The {@link ProgressListener} may be used to display a progress bar
 * or do stuff like that.
 */
public interface ProgressListener {

    /**
     * Updates the listeners status information.
     *
     * @param pBytesRead The total number of bytes, which have been read
     *   so far.
     * @param pContentLength The total number of bytes, which are being
     *   read. May be -1, if this number is unknown.
     * @param pItems The number of the field, which is currently being
     *   read. (0 = no item so far, 1 = first item is being read, ...)
     */
    void update(long pBytesRead, long pContentLength, int pItems);

}
