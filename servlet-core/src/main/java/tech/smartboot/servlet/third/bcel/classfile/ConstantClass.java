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
 * and represents a reference to a (external) class.
 *
 * @see     Constant
 */
public final class ConstantClass extends Constant {

    private final int name_index; // Identical to ConstantString except for the name


    /**
     * Constructs an instance from file data.
     *
     * @param dataInput Input stream
     * @throws IOException if an I/O error occurs reading from the given {@code dataInput}.
     */
    ConstantClass(final DataInput dataInput) throws IOException {
        super(Const.CONSTANT_Class);
        this.name_index = dataInput.readUnsignedShort();
    }


    /**
     * @return Name index in constant pool of class name.
     */
    public final int getNameIndex() {
        return name_index;
    }
}
