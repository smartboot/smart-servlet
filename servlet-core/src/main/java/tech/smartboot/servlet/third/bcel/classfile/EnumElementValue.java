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

public class EnumElementValue extends ElementValue
{
    private final int valueIdx;

    EnumElementValue(final int type, final int valueIdx, final ConstantPool cpool) {
        super(type, cpool);
        if (type != ENUM_CONSTANT)
            throw new RuntimeException(
                    "Only element values of type enum can be built with this ctor - type specified: " + type);
        this.valueIdx = valueIdx;
    }

    @Override
    public String stringifyValue()
    {
        final ConstantUtf8 cu8 = (ConstantUtf8) super.getConstantPool().getConstant(valueIdx,
                Const.CONSTANT_Utf8);
        return cu8.getBytes();
    }
}
