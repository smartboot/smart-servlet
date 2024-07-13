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
 * This class is derived from the abstract {@link Constant}
 * and represents a reference to a long object.
 *
 * @see     Constant
 */
public final class ConstantLong extends Constant {

    private final long bytes;


    /**
     * Initialize instance from file data.
     *
     * @param file Input stream
     * @throws IOException
     */
    ConstantLong(final DataInput input) throws IOException {
        super(Const.CONSTANT_Long);
        this.bytes = input.readLong();
    }


    /**
     * @return data, i.e., 8 bytes.
     */
    public final long getBytes() {
        return bytes;
    }
}
