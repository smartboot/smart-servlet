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
 * Abstract superclass for classes to represent the different constant types
 * in the constant pool of a class file. The classes keep closely to
 * the JVM specification.
 *
 * @author  <A HREF="mailto:m.dahm@gmx.de">M. Dahm</A>
 */
public abstract class Constant {

    /* In fact this tag is redundant since we can distinguish different
     * `Constant' objects by their type, i.e., via `instanceof'. In some
     * places we will use the tag for switch()es anyway.
     *
     * First, we want match the specification as closely as possible. Second we
     * need the tag as an index to select the corresponding class name from the
     * `CONSTANT_NAMES' array.
     */
    protected final byte tag;


    Constant(final byte tag) {
        this.tag = tag;
    }


    /**
     * @return Tag of constant, i.e., its type. No setTag() method to avoid
     * confusion.
     */
    public final byte getTag() {
        return tag;
    }


    /**
     * Read one constant from the given input, the type depends on a tag byte.
     *
     * @param dataInput Input stream
     * @return Constant object
     * @throws IOException if an I/O error occurs reading from the given {@code dataInput}.
     * @throws ClassFormatException if the next byte is not recognized
     */
    static Constant readConstant(final DataInput dataInput) throws IOException, ClassFormatException {
        final byte b = dataInput.readByte(); // Read tag byte
        int skipSize;
        switch (b) {
            case Const.CONSTANT_Class:
                return new ConstantClass(dataInput);
            case Const.CONSTANT_Integer:
                return new ConstantInteger(dataInput);
            case Const.CONSTANT_Float:
                return new ConstantFloat(dataInput);
            case Const.CONSTANT_Long:
                return new ConstantLong(dataInput);
            case Const.CONSTANT_Double:
                return new ConstantDouble(dataInput);
            case Const.CONSTANT_Utf8:
                return ConstantUtf8.getInstance(dataInput);
            case Const.CONSTANT_String:
            case Const.CONSTANT_MethodType:
            case Const.CONSTANT_Module:
            case Const.CONSTANT_Package:
                skipSize = 2; // unsigned short
                break;
            case Const.CONSTANT_MethodHandle:
                skipSize = 3; // unsigned byte, unsigned short
                break;
            case Const.CONSTANT_Fieldref:
            case Const.CONSTANT_Methodref:
            case Const.CONSTANT_InterfaceMethodref:
            case Const.CONSTANT_NameAndType:
            case Const.CONSTANT_Dynamic:
            case Const.CONSTANT_InvokeDynamic:
                skipSize = 4; // unsigned short, unsigned short
                break;
            default:
                throw new ClassFormatException("Invalid byte tag in constant pool: " + b);
        }
        Utility.skipFully(dataInput, skipSize);
        return null;
    }

    @Override
    public String toString() {
        return "[" + tag + "]";
    }
}
