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
 * A file filter that always returns true.
 *
 * @since 1.0
 * @version $Id: TrueFileFilter.java 1304058 2012-03-22 21:02:43Z sebb $
 * @see FileFilterUtils#trueFileFilter()
 */
public class TrueFileFilter implements IOFileFilter, Serializable {

    /**
     * Singleton instance of true filter.
     * @since 1.3
     */
    public static final IOFileFilter TRUE = new TrueFileFilter();
    /**
     * Singleton instance of true filter.
     * Please use the identical TrueFileFilter.TRUE constant.
     * The new name is more JDK 1.5 friendly as it doesn't clash with other
     * values when using static imports.
     */
    public static final IOFileFilter INSTANCE = TRUE;

    /**
     * Restrictive consructor.
     */
    protected TrueFileFilter() {
    }

    /**
     * Returns true.
     *
     * @param file  the file to check (ignored)
     * @return true
     */
    public boolean accept(File file) {
        return true;
    }

    /**
     * Returns true.
     *
     * @param dir  the directory to check (ignored)
     * @param name  the filename (ignored)
     * @return true
     */
    public boolean accept(File dir, String name) {
        return true;
    }

}
