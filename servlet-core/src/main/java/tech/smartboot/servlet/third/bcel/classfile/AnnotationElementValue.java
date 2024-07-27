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

public class AnnotationElementValue extends ElementValue
{
    // For annotation element values, this is the annotation
    private final AnnotationEntry annotationEntry;

    AnnotationElementValue(final int type, final AnnotationEntry annotationEntry,
            final ConstantPool cpool)
    {
        super(type, cpool);
        if (type != ANNOTATION) {
            throw new RuntimeException(
                    "Only element values of type annotation can be built with this ctor - type specified: " + type);
        }
        this.annotationEntry = annotationEntry;
    }

    @Override
    public String stringifyValue()
    {
        return annotationEntry.toString();
    }

    public AnnotationEntry getAnnotationEntry()
    {
        return annotationEntry;
    }
}
