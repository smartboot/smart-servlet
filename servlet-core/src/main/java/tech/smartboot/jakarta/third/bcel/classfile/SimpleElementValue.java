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

public class SimpleElementValue extends ElementValue
{
    private final int index;

    SimpleElementValue(final int type, final int index, final ConstantPool cpool) {
        super(type, cpool);
        this.index = index;
    }

    /**
     * @return Value entry index in the cpool
     */
    public int getIndex()
    {
        return index;
    }


    // Whatever kind of value it is, return it as a string
    @Override
    public String stringifyValue()
    {
        final ConstantPool cpool = super.getConstantPool();
        final int _type = super.getType();
        switch (_type)
        {
        case PRIMITIVE_INT:
            final ConstantInteger c = (ConstantInteger) cpool.getConstant(getIndex(),
                    Const.CONSTANT_Integer);
            return Integer.toString(c.getBytes());
        case PRIMITIVE_LONG:
            final ConstantLong j = (ConstantLong) cpool.getConstant(getIndex(),
                    Const.CONSTANT_Long);
            return Long.toString(j.getBytes());
        case PRIMITIVE_DOUBLE:
            final ConstantDouble d = (ConstantDouble) cpool.getConstant(getIndex(),
                    Const.CONSTANT_Double);
            return Double.toString(d.getBytes());
        case PRIMITIVE_FLOAT:
            final ConstantFloat f = (ConstantFloat) cpool.getConstant(getIndex(),
                    Const.CONSTANT_Float);
            return Float.toString(f.getBytes());
        case PRIMITIVE_SHORT:
            final ConstantInteger s = (ConstantInteger) cpool.getConstant(getIndex(),
                    Const.CONSTANT_Integer);
            return Integer.toString(s.getBytes());
        case PRIMITIVE_BYTE:
            final ConstantInteger b = (ConstantInteger) cpool.getConstant(getIndex(),
                    Const.CONSTANT_Integer);
            return Integer.toString(b.getBytes());
        case PRIMITIVE_CHAR:
            final ConstantInteger ch = (ConstantInteger) cpool.getConstant(
                    getIndex(), Const.CONSTANT_Integer);
            return String.valueOf((char)ch.getBytes());
        case PRIMITIVE_BOOLEAN:
            final ConstantInteger bo = (ConstantInteger) cpool.getConstant(
                    getIndex(), Const.CONSTANT_Integer);
            if (bo.getBytes() == 0) {
                return "false";
            }
            return "true";
        case STRING:
            final ConstantUtf8 cu8 = (ConstantUtf8) cpool.getConstant(getIndex(),
                    Const.CONSTANT_Utf8);
            return cu8.getBytes();
        default:
            throw new RuntimeException("SimpleElementValue class does not know how to stringify type " + _type);
        }
    }
}
