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

import java.io.File;
import java.io.Serializable;

/**
 * A file filter that always returns false.
 *
 * @since 1.0
 * @version $Id: FalseFileFilter.java 1304058 2012-03-22 21:02:43Z sebb $
 *
 * @see FileFilterUtils#falseFileFilter()
 */
public class FalseFileFilter implements IOFileFilter, Serializable {

    /**
     * Singleton instance of false filter.
     * @since 1.3
     */
    public static final IOFileFilter FALSE = new FalseFileFilter();
    /**
     * Singleton instance of false filter.
     * Please use the identical FalseFileFilter.FALSE constant.
     * The new name is more JDK 1.5 friendly as it doesn't clash with other
     * values when using static imports.
     */
    public static final IOFileFilter INSTANCE = FALSE;

    /**
     * Restrictive consructor.
     */
    protected FalseFileFilter() {
    }

    /**
     * Returns false.
     *
     * @param file  the file to check (ignored)
     * @return false
     */
    public boolean accept(File file) {
        return false;
    }

    /**
     * Returns false.
     *
     * @param dir  the directory to check (ignored)
     * @param name  the filename (ignored)
     * @return false
     */
    public boolean accept(File dir, String name) {
        return false;
    }

}
