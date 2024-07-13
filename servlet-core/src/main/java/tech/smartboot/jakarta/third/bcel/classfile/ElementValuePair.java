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
 * an annotation's element value pair
 *
 * @since 6.0
 */
public class ElementValuePair
{
    private final ElementValue elementValue;

    private final ConstantPool constantPool;

    private final int elementNameIndex;

    ElementValuePair(final DataInput file, final ConstantPool constantPool) throws IOException {
        this.constantPool = constantPool;
        this.elementNameIndex = file.readUnsignedShort();
        this.elementValue = ElementValue.readElementValue(file, constantPool);
    }

    public String getNameString()
    {
        final ConstantUtf8 c = (ConstantUtf8) constantPool.getConstant(
                elementNameIndex, Const.CONSTANT_Utf8);
        return c.getBytes();
    }

    public final ElementValue getValue()
    {
        return elementValue;
    }
}
