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

/**
 * Represents a Java class, i.e., the data structures, constant pool,
 * fields, methods and commands contained in a Java .class file.
 * See <a href="http://docs.oracle.com/javase/specs/">JVM specification</a> for details.
 * The intent of this class is to represent a parsed or otherwise existing
 * class file.  Those interested in programatically generating classes
 * should see the <a href="../generic/ClassGen.html">ClassGen</a> class.
 */
public class JavaClass {

    private final int access_flags;
    private final String class_name;
    private final String superclass_name;
    private final String[] interface_names;
    private final Annotations runtimeVisibleAnnotations; // "RuntimeVisibleAnnotations" attribute defined in the class

    /**
     * Constructor gets all contents as arguments.
     *
     * @param class_name Name of this class.
     * @param superclass_name Name of this class's superclass.
     * @param access_flags Access rights defined by bit flags
     * @param constant_pool Array of constants
     * @param interfaces Implemented interfaces
     * @param runtimeVisibleAnnotations "RuntimeVisibleAnnotations" attribute defined on the Class, or null
     */
    JavaClass(final String class_name, final String superclass_name,
            final int access_flags, final ConstantPool constant_pool, final String[] interface_names,
            final Annotations runtimeVisibleAnnotations) {
        this.access_flags = access_flags;
        this.runtimeVisibleAnnotations = runtimeVisibleAnnotations;
        this.class_name = class_name;
        this.superclass_name = superclass_name;
        this.interface_names = interface_names;
    }

    /**
     * @return Access flags of the object aka. "modifiers".
     */
    public final int getAccessFlags() {
        return access_flags;
    }

    /**
     * Return annotations entries from "RuntimeVisibleAnnotations" attribute on
     * the class, if there is any.
     *
     * @return An array of entries or {@code null}
     */
    public AnnotationEntry[] getAnnotationEntries() {
        if (runtimeVisibleAnnotations != null) {
            return runtimeVisibleAnnotations.getAnnotationEntries();
        }
        return null;
    }

    /**
     * @return Class name.
     */
    public String getClassName() {
        return class_name;
    }


    /**
     * @return Names of implemented interfaces.
     */
    public String[] getInterfaceNames() {
        return interface_names;
    }


    /**
     * returns the super class name of this class. In the case that this class is
     * java.lang.Object, it will return itself (java.lang.Object). This is probably incorrect
     * but isn't fixed at this time to not break existing clients.
     *
     * @return Superclass name.
     */
    public String getSuperclassName() {
        return superclass_name;
    }
}
