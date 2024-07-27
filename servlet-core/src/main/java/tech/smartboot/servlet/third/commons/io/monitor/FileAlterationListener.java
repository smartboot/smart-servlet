/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.servlet.third.commons.io.monitor;

import java.io.File;

/**
 * A listener that receives events of file system modifications.
 * <p>
 * Register {@link FileAlterationListener}s with a {@link FileAlterationObserver}.
 * 
 * @see FileAlterationObserver
 * @version $Id: FileAlterationListener.java 1304052 2012-03-22 20:55:29Z ggregory $
 * @since 2.0
 */
public interface FileAlterationListener {

    /**
     * File system observer started checking event.
     *
     * @param observer The file system observer
     */
    void onStart(final FileAlterationObserver observer);

    /**
     * Directory created Event.
     * 
     * @param directory The directory created
     */
    void onDirectoryCreate(final File directory);

    /**
     * Directory changed Event.
     * 
     * @param directory The directory changed
     */
    void onDirectoryChange(final File directory);

    /**
     * Directory deleted Event.
     * 
     * @param directory The directory deleted
     */
    void onDirectoryDelete(final File directory);

    /**
     * File created Event.
     * 
     * @param file The file created
     */
    void onFileCreate(final File file);

    /**
     * File changed Event.
     * 
     * @param file The file changed
     */
    void onFileChange(final File file);

    /**
     * File deleted Event.
     * 
     * @param file The file deleted
     */
    void onFileDelete(final File file);

    /**
     * File system observer finished checking event.
     *
     * @param observer The file system observer
     */
    void onStop(final FileAlterationObserver observer);
}
