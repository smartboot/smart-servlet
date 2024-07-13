/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.jakarta.third.bcel;

/**
 * Constants for the project, mostly defined in the JVM specification.
 */
public final class Const {

    /** One of the access flags for fields, methods, or classes.
     *  @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.5">
     *  Flag definitions for Fields in the Java Virtual Machine Specification (Java SE 8 Edition).</a>
     *  @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.6">
     *  Flag definitions for Methods in the Java Virtual Machine Specification (Java SE 8 Edition).</a>
     *  @see <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.6-300-D.1-D.1">
     *  Flag definitions for Classes in the Java Virtual Machine Specification (Java SE 8 Edition).</a>
     */
    public static final short ACC_FINAL      = 0x0010;

    /** One of the access flags for fields, methods, or classes.
     */
    public static final short ACC_INTERFACE    = 0x0200;

    /** One of the access flags for fields, methods, or classes.
     */
    public static final short ACC_ABSTRACT     = 0x0400;

    /** One of the access flags for fields, methods, or classes.
     */
    public static final short ACC_ANNOTATION   = 0x2000;

    /**
     * Marks a constant pool entry as type UTF-8.
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.7">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_Utf8               = 1;

    /**
     * Marks a constant pool entry as type Integer.
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.4">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_Integer            = 3;

    /**
     * Marks a constant pool entry as type Float.
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.4">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_Float              = 4;

    /**
     * Marks a constant pool entry as type Long.
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.5">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_Long               = 5;

    /**
     * Marks a constant pool entry as type Double.
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.5">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_Double             = 6;

    /**
     * Marks a constant pool entry as a Class
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.1">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_Class              = 7;

    /**
     * Marks a constant pool entry as type String
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.3">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_String             = 8;

    /**
     * Marks a constant pool entry as a Field Reference.
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.2">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_Fieldref           = 9;

    /**
     * Marks a constant pool entry as a Method Reference.
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.2">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_Methodref          = 10;

    /**
     * Marks a constant pool entry as an Interface Method Reference.
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.2">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_InterfaceMethodref = 11;

    /**
     * Marks a constant pool entry as a name and type.
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.6">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_NameAndType        = 12;

    /**
     * Marks a constant pool entry as a Method Handle.
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.8">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_MethodHandle       = 15;

    /**
     * Marks a constant pool entry as a Method Type.
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.9">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_MethodType         = 16;

    /**
     * Marks a constant pool entry as dynamically computed.
     * @see  <a href="https://bugs.openjdk.java.net/secure/attachment/74618/constant-dynamic.html">
     * Change request for JEP 309</a>
     */
    public static final byte CONSTANT_Dynamic            = 17;

    /**
     * Marks a constant pool entry as an Invoke Dynamic
     * @see  <a href="http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.10">
     * The Constant Pool in The Java Virtual Machine Specification</a>
     */
    public static final byte CONSTANT_InvokeDynamic      = 18;

    /**
     * Marks a constant pool entry as a Module Reference.
     *
     * <p>Note: Early access Java 9 support- currently subject to change</p>
     *
     * @see <a href="http://cr.openjdk.java.net/~mr/jigsaw/spec/lang-vm.html#jigsaw-2.6">
     * JPMS: Modules in the Java Language and JVM</a>
     */
    public static final byte CONSTANT_Module             = 19;

    /**
     * Marks a constant pool entry as a Package Reference.
     *
     * <p>Note: Early access Java 9 support- currently subject to change</p>
     *
     * @see <a href="http://cr.openjdk.java.net/~mr/jigsaw/spec/lang-vm.html#jigsaw-2.6">
     * JPMS: Modules in the Java Language and JVM</a>
     */
    public static final byte CONSTANT_Package            = 20;

    /**
     * The names of the types of entries in a constant pool.
     * Use getConstantName instead
     */
    private static final String[] CONSTANT_NAMES = {
    "", "CONSTANT_Utf8", "", "CONSTANT_Integer",
    "CONSTANT_Float", "CONSTANT_Long", "CONSTANT_Double",
    "CONSTANT_Class", "CONSTANT_String", "CONSTANT_Fieldref",
    "CONSTANT_Methodref", "CONSTANT_InterfaceMethodref",
    "CONSTANT_NameAndType", "", "", "CONSTANT_MethodHandle",
    "CONSTANT_MethodType", "CONSTANT_Dynamic", "CONSTANT_InvokeDynamic",
    "CONSTANT_Module", "CONSTANT_Package"};

    public static String getConstantName(int index) {
        return CONSTANT_NAMES[index];
    }
}
