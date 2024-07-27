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

import tech.smartboot.servlet.third.bcel.Const;

import java.io.DataInput;
import java.io.IOException;

/**
 * This class is derived from the abstract {@link Constant}
 * and represents a reference to an int object.
 *
 * @see     Constant
 */
public final class ConstantInteger extends Constant {

    private final int bytes;


    /**
     * Initialize instance from file data.
     *
     * @param file Input stream
     * @throws IOException
     */
    ConstantInteger(final DataInput file) throws IOException {
        super(Const.CONSTANT_Integer);
        this.bytes = file.readInt();
    }


    /**
     * @return data, i.e., 4 bytes.
     */
    public final int getBytes() {
        return bytes;
    }
}
