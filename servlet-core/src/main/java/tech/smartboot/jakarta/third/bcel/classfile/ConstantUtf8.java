/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.jakarta.third.bcel.classfile;

import tech.smartboot.jakarta.third.bcel.Const;

import java.io.DataInput;
import java.io.IOException;

/**
 * This class is derived from the abstract
 * <A HREF="org.apache.tomcat.util.bcel.classfile.Constant.html">Constant</A> class
 * and represents a reference to a Utf8 encoded string.
 *
 * @see     Constant
 */
public final class ConstantUtf8 extends Constant {

    private final String bytes;


    static ConstantUtf8 getInstance(final DataInput input) throws IOException {
        return new ConstantUtf8(input.readUTF());
    }


    /**
     * @param bytes Data
     */
    private ConstantUtf8(final String bytes) {
        super(Const.CONSTANT_Utf8);
        if (bytes == null) {
            throw new IllegalArgumentException("bytes must not be null!");
        }
        this.bytes = bytes;
    }


    /**
     * @return Data converted to string.
     */
    public final String getBytes() {
        return bytes;
    }
}
