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

import tech.smartboot.jakarta.third.commons.io.FilenameUtils;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Filters files using the supplied wildcards.
 * <p>
 * This filter selects files, but not directories, based on one or more wildcards
 * and using case-sensitive comparison.
 * <p>
 * The wildcard matcher uses the characters '?' and '*' to represent a
 * single or multiple wildcard characters.
 * This is the same as often found on Dos/Unix command lines.
 * The extension check is case-sensitive.
 * See {@link FilenameUtils#wildcardMatch(String, String)} for more information.
 * <p>
 * For example:
 * <pre>
 * File dir = new File(".");
 * FileFilter fileFilter = new WildcardFilter("*test*.java~*~");
 * File[] files = dir.listFiles(fileFilter);
 * for (int i = 0; i < files.length; i++) {
 *   System.out.println(files[i]);
 * }
 * </pre>
 *
 * @version $Id: WildcardFilter.java 1303950 2012-03-22 18:16:04Z ggregory $
 * @since 1.1
 * @deprecated Use WilcardFileFilter. Deprecated as this class performs directory
 * filtering which it shouldn't do, but that can't be removed due to compatability.
 */
@Deprecated
public class WildcardFilter extends AbstractFileFilter implements Serializable {

    /** The wildcards that will be used to match filenames. */
    private final String[] wildcards;

    /**
     * Construct a new case-sensitive wildcard filter for a single wildcard.
     *
     * @param wildcard  the wildcard to match
     * @throws IllegalArgumentException if the pattern is null
     */
    public WildcardFilter(String wildcard) {
        if (wildcard == null) {
            throw new IllegalArgumentException("The wildcard must not be null");
        }
        this.wildcards = new String[] { wildcard };
    }

    /**
     * Construct a new case-sensitive wildcard filter for an array of wildcards.
     *
     * @param wildcards  the array of wildcards to match
     * @throws IllegalArgumentException if the pattern array is null
     */
    public WildcardFilter(String[] wildcards) {
        if (wildcards == null) {
            throw new IllegalArgumentException("The wildcard array must not be null");
        }
        this.wildcards = new String[wildcards.length];
        System.arraycopy(wildcards, 0, this.wildcards, 0, wildcards.length);
    }

    /**
     * Construct a new case-sensitive wildcard filter for a list of wildcards.
     *
     * @param wildcards  the list of wildcards to match
     * @throws IllegalArgumentException if the pattern list is null
     * @throws ClassCastException if the list does not contain Strings
     */
    public WildcardFilter(List<String> wildcards) {
        if (wildcards == null) {
            throw new IllegalArgumentException("The wildcard list must not be null");
        }
        this.wildcards = wildcards.toArray(new String[wildcards.size()]);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks to see if the filename matches one of the wildcards.
     *
     * @param dir  the file directory
     * @param name  the filename
     * @return true if the filename matches one of the wildcards
     */
    @Override
    public boolean accept(File dir, String name) {
        if (dir != null && new File(dir, name).isDirectory()) {
            return false;
        }

        for (String wildcard : wildcards) {
            if (FilenameUtils.wildcardMatch(name, wildcard)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Checks to see if the filename matches one of the wildcards.
     *
     * @param file the file to check
     * @return true if the filename matches one of the wildcards
     */
    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return false;
        }

        for (String wildcard : wildcards) {
            if (FilenameUtils.wildcardMatch(file.getName(), wildcard)) {
                return true;
            }
        }
        
        return false;
    }

}
