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

import java.io.DataInput;
import java.io.IOException;

/**
 * base class for annotations
 */
public class Annotations {

    private final AnnotationEntry[] annotation_table;

    /**
     * @param input Input stream
     * @param constant_pool Array of constants
     */
    Annotations(final DataInput input, final ConstantPool constant_pool) throws IOException {
        final int annotation_table_length = input.readUnsignedShort();
        annotation_table = new AnnotationEntry[annotation_table_length];
        for (int i = 0; i < annotation_table_length; i++) {
            annotation_table[i] = new AnnotationEntry(input, constant_pool);
        }
    }


    /**
     * @return the array of annotation entries in this annotation
     */
    public AnnotationEntry[] getAnnotationEntries() {
        return annotation_table;
    }
}
