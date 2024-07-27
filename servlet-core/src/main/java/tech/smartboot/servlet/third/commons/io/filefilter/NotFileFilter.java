/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.servlet.third.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;

/**
 * This filter produces a logical NOT of the filters specified.
 *
 * @since 1.0
 * @version $Id: NotFileFilter.java 1304052 2012-03-22 20:55:29Z ggregory $
 * @see FileFilterUtils#notFileFilter(IOFileFilter)
 */
public class NotFileFilter extends AbstractFileFilter implements Serializable {
    
    /** The filter */
    private final IOFileFilter filter;

    /**
     * Constructs a new file filter that NOTs the result of another filter.
     * 
     * @param filter  the filter, must not be null
     * @throws IllegalArgumentException if the filter is null
     */
    public NotFileFilter(IOFileFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("The filter must not be null");
        }
        this.filter = filter;
    }

    /**
     * Returns the logical NOT of the underlying filter's return value for the same File.
     * 
     * @param file  the File to check
     * @return true if the filter returns false
     */
    @Override
    public boolean accept(File file) {
        return ! filter.accept(file);
    }
    
    /**
     * Returns the logical NOT of the underlying filter's return value for the same arguments.
     * 
     * @param file  the File directory
     * @param name  the filename
     * @return true if the filter returns false
     */
    @Override
    public boolean accept(File file, String name) {
        return ! filter.accept(file, name);
    }

    /**
     * Provide a String representaion of this file filter.
     *
     * @return a String representaion
     */
    @Override
    public String toString() {
        return super.toString() + "(" + filter.toString()  + ")";
    }
    
}
