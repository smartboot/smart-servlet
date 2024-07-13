/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.jakarta.third.commons.io.filefilter;

import tech.smartboot.jakarta.third.commons.io.IOCase;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Filters filenames for a certain name.
 * <p>
 * For example, to print all files and directories in the 
 * current directory whose name is <code>Test</code>:
 *
 * <pre>
 * File dir = new File(".");
 * String[] files = dir.list( new NameFileFilter("Test") );
 * for ( int i = 0; i &lt; files.length; i++ ) {
 *     System.out.println(files[i]);
 * }
 * </pre>
 *
 * @since 1.0
 * @version $Id: NameFileFilter.java 1304058 2012-03-22 21:02:43Z sebb $
 * @see FileFilterUtils#nameFileFilter(String)
 * @see FileFilterUtils#nameFileFilter(String, IOCase)
 */
public class NameFileFilter extends AbstractFileFilter implements Serializable {
    
    /** The filenames to search for */
    private final String[] names;
    /** Whether the comparison is case sensitive. */
    private final IOCase caseSensitivity;

    /**
     * Constructs a new case-sensitive name file filter for a single name.
     * 
     * @param name  the name to allow, must not be null
     * @throws IllegalArgumentException if the name is null
     */
    public NameFileFilter(String name) {
        this(name, null);
    }

    /**
     * Construct a new name file filter specifying case-sensitivity.
     *
     * @param name  the name to allow, must not be null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the name is null
     */
    public NameFileFilter(String name, IOCase caseSensitivity) {
        if (name == null) {
            throw new IllegalArgumentException("The wildcard must not be null");
        }
        this.names = new String[] {name};
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Constructs a new case-sensitive name file filter for an array of names.
     * <p>
     * The array is not cloned, so could be changed after constructing the
     * instance. This would be inadvisable however.
     * 
     * @param names  the names to allow, must not be null
     * @throws IllegalArgumentException if the names array is null
     */
    public NameFileFilter(String[] names) {
        this(names, null);
    }

    /**
     * Constructs a new name file filter for an array of names specifying case-sensitivity.
     * <p>
     * The array is not cloned, so could be changed after constructing the
     * instance. This would be inadvisable however.
     * 
     * @param names  the names to allow, must not be null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the names array is null
     */
    public NameFileFilter(String[] names, IOCase caseSensitivity) {
        if (names == null) {
            throw new IllegalArgumentException("The array of names must not be null");
        }
        this.names = new String[names.length];
        System.arraycopy(names, 0, this.names, 0, names.length);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    /**
     * Constructs a new case-sensitive name file filter for a list of names.
     * 
     * @param names  the names to allow, must not be null
     * @throws IllegalArgumentException if the name list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public NameFileFilter(List<String> names) {
        this(names, null);
    }

    /**
     * Constructs a new name file filter for a list of names specifying case-sensitivity.
     * 
     * @param names  the names to allow, must not be null
     * @param caseSensitivity  how to handle case sensitivity, null means case-sensitive
     * @throws IllegalArgumentException if the name list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public NameFileFilter(List<String> names, IOCase caseSensitivity) {
        if (names == null) {
            throw new IllegalArgumentException("The list of names must not be null");
        }
        this.names = names.toArray(new String[names.size()]);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks to see if the filename matches.
     * 
     * @param file  the File to check
     * @return true if the filename matches
     */
    @Override
    public boolean accept(File file) {
        String name = file.getName();
        for (String name2 : this.names) {
            if (caseSensitivity.checkEquals(name, name2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the filename matches.
     * 
     * @param dir  the File directory (ignored)
     * @param name  the filename
     * @return true if the filename matches
     */
    @Override
    public boolean accept(File dir, String name) {
        for (String name2 : names) {
            if (caseSensitivity.checkEquals(name, name2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Provide a String representaion of this file filter.
     *
     * @return a String representaion
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(super.toString());
        buffer.append("(");
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(names[i]);
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

}
