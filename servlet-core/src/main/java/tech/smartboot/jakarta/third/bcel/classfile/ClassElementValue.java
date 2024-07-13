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

public class ClassElementValue extends ElementValue
{
    // For primitive types and string type, this points to the value entry in
    // the cpool
    // For 'class' this points to the class entry in the cpool
    private final int idx;

    ClassElementValue(final int type, final int idx, final ConstantPool cpool) {
        super(type, cpool);
        this.idx = idx;
    }


    @Override
    public String stringifyValue()
    {
        final ConstantUtf8 cu8 = (ConstantUtf8) super.getConstantPool().getConstant(idx,
                Const.CONSTANT_Utf8);
        return cu8.getBytes();
    }
}
