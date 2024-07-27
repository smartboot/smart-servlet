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
import java.util.ArrayList;
import java.util.List;

/**
 * represents one annotation in the annotation table
 */
public class AnnotationEntry {

    private final int type_index;
    private final ConstantPool constant_pool;

    private final List<ElementValuePair> element_value_pairs;

    /*
     * Creates an AnnotationEntry from a DataInputStream
     *
     * @param input
     * @param constant_pool
     * @throws IOException
     */
    AnnotationEntry(final DataInput input, final ConstantPool constant_pool) throws IOException {

        this.constant_pool = constant_pool;

        type_index = input.readUnsignedShort();
        final int num_element_value_pairs = input.readUnsignedShort();

        element_value_pairs = new ArrayList<>(num_element_value_pairs);
        for (int i = 0; i < num_element_value_pairs; i++) {
            element_value_pairs.add(new ElementValuePair(input, constant_pool));
        }
    }

    /**
     * @return the annotation type name
     */
    public String getAnnotationType() {
        final ConstantUtf8 c = (ConstantUtf8) constant_pool.getConstant(type_index, Const.CONSTANT_Utf8);
        return c.getBytes();
    }

    /**
     * @return the element value pairs in this annotation entry
     */
    public List<ElementValuePair> getElementValuePairs() {
        return element_value_pairs;
    }
}
