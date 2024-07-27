/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.servlet.third.bcel.classfile;

/**
 * Thrown when the BCEL attempts to read a class file and determines
 * that the file is malformed or otherwise cannot be interpreted as a
 * class file.
 */
public class ClassFormatException extends RuntimeException {

    private static final long serialVersionUID = 3243149520175287759L;

    public ClassFormatException() {
        super();
    }


    public ClassFormatException(final String s) {
        super(s);
    }
}
