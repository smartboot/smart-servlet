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

import java.util.List;

/**
 * Defines operations for conditional file filters.
 *
 * @since 1.1
 * @version $Id: ConditionalFileFilter.java 1303950 2012-03-22 18:16:04Z ggregory $
 */
public interface ConditionalFileFilter {

    /**
     * Adds the specified file filter to the list of file filters at the end of
     * the list.
     *
     * @param ioFileFilter the filter to be added
     * @since 1.1
     */
    void addFileFilter(IOFileFilter ioFileFilter);

    /**
     * Returns this conditional file filter's list of file filters.
     *
     * @return the file filter list
     * @since 1.1
     */
    List<IOFileFilter> getFileFilters();

    /**
     * Removes the specified file filter.
     *
     * @param ioFileFilter filter to be removed
     * @return <code>true</code> if the filter was found in the list,
     * <code>false</code> otherwise
     * @since 1.1
     */
    boolean removeFileFilter(IOFileFilter ioFileFilter);

    /**
     * Sets the list of file filters, replacing any previously configured
     * file filters on this filter.
     *
     * @param fileFilters the list of filters
     * @since 1.1
     */
    void setFileFilters(List<IOFileFilter> fileFilters);

}
