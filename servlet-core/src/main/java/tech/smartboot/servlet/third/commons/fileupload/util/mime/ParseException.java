/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.servlet.third.commons.fileupload.util.mime;

/**
 * @since 1.3
 */
final class ParseException extends Exception {

    /**
     * The UID to use when serializing this instance.
     */
    private static final long serialVersionUID = 5355281266579392077L;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public ParseException(String message) {
        super(message);
    }

}
