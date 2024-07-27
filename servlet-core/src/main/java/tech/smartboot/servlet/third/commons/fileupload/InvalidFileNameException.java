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
 * This exception is thrown in case of an invalid file name.
 * A file name is invalid, if it contains a NUL character.
 * Attackers might use this to circumvent security checks:
 * For example, a malicious user might upload a file with the name
 * "foo.exe\0.png". This file name might pass security checks (i.e.
 * checks for the extension ".png"), while, depending on the underlying
 * C library, it might create a file named "foo.exe", as the NUL
 * character is the string terminator in C.
 */
public class InvalidFileNameException extends RuntimeException {

    /**
     * Serial version UID, being used, if the exception
     * is serialized.
     */
    private static final long serialVersionUID = 7922042602454350470L;

    /**
     * The file name causing the exception.
     */
    private final String name;

    /**
     * Creates a new instance.
     *
     * @param pName The file name causing the exception.
     * @param pMessage A human readable error message.
     */
    public InvalidFileNameException(String pName, String pMessage) {
        super(pMessage);
        name = pName;
    }

    /**
     * Returns the invalid file name.
     *
     * @return the invalid file name.
     */
    public String getName() {
        return name;
    }

}
