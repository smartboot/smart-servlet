/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.servlet.third.commons.fileupload.disk;

import tech.smartboot.servlet.third.commons.io.FileCleaningTracker;
import tech.smartboot.servlet.third.commons.fileupload.FileItem;
import tech.smartboot.servlet.third.commons.fileupload.FileItemFactory;

import java.io.File;

/**
 * <p>The default {@link FileItemFactory}
 * implementation. This implementation creates
 * {@link FileItem} instances which keep their
 * content either in memory, for smaller items, or in a temporary file on disk,
 * for larger items. The size threshold, above which content will be stored on
 * disk, is configurable, as is the directory in which temporary files will be
 * created.</p>
 *
 * <p>If not otherwise configured, the default configuration values are as
 * follows:</p>
 * <ul>
 *   <li>Size threshold is 10KB.</li>
 *   <li>Repository is the system default temp directory, as returned by
 *       <code>System.getProperty("java.io.tmpdir")</code>.</li>
 * </ul>
 * <p>
 * <b>NOTE</b>: Files are created in the system default temp directory with
 * predictable names. This means that a local attacker with write access to that
 * directory can perform a TOUTOC attack to replace any uploaded file with a
 * file of the attackers choice. The implications of this will depend on how the
 * uploaded file is used but could be significant. When using this
 * implementation in an environment with local, untrusted users,
 * {@link #setRepository(File)} MUST be used to configure a repository location
 * that is not publicly writable. In a Servlet container the location identified
 * by the ServletContext attribute <code>javax.servlet.context.tempdir</code>
 * may be used.
 * </p>
 *
 * <p>Temporary files, which are created for file items, should be
 * deleted later on. The best way to do this is using a
 * {@link FileCleaningTracker}, which you can set on the
 * {@link DiskFileItemFactory}. However, if you do use such a tracker,
 * then you must consider the following: Temporary files are automatically
 * deleted as soon as they are no longer needed. (More precisely, when the
 * corresponding instance of {@link File} is garbage collected.)
 * This is done by the so-called reaper thread, which is started and stopped
 * automatically by the {@link FileCleaningTracker} when there are files to be
 * tracked.
 * It might make sense to terminate that thread, for example, if
 * your web application ends. See the section on "Resource cleanup"
 * in the users guide of commons-fileupload.</p>
 *
 * @since FileUpload 1.1
 */
public class DiskFileItemFactory implements FileItemFactory {

    // ----------------------------------------------------- Manifest constants

    /**
     * The default threshold above which uploads will be stored on disk.
     */
    public static final int DEFAULT_SIZE_THRESHOLD = 10240;

    // ----------------------------------------------------- Instance Variables

    /**
     * The directory in which uploaded files will be stored, if stored on disk.
     */
    private File repository;

    /**
     * The threshold above which uploads will be stored on disk.
     */
    private int sizeThreshold = DEFAULT_SIZE_THRESHOLD;

    /**
     * <p>The instance of {@link FileCleaningTracker}, which is responsible
     * for deleting temporary files.</p>
     * <p>May be null, if tracking files is not required.</p>
     */
    private FileCleaningTracker fileCleaningTracker;

    /**
     * Default content charset to be used when no explicit charset
     * parameter is provided by the sender.
     */
    private String defaultCharset = DiskFileItem.DEFAULT_CHARSET;

    // ----------------------------------------------------------- Constructors

    /**
     * Constructs an unconfigured instance of this class. The resulting factory
     * may be configured by calling the appropriate setter methods.
     */
    public DiskFileItemFactory() {
        this(DEFAULT_SIZE_THRESHOLD, null);
    }

    /**
     * Constructs a preconfigured instance of this class.
     *
     * @param sizeThreshold The threshold, in bytes, below which items will be
     *                      retained in memory and above which they will be
     *                      stored as a file.
     * @param repository    The data repository, which is the directory in
     *                      which files will be created, should the item size
     *                      exceed the threshold.
     */
    public DiskFileItemFactory(int sizeThreshold, File repository) {
        this.sizeThreshold = sizeThreshold;
        this.repository = repository;
    }

    // ------------------------------------------------------------- Properties

    /**
     * Returns the directory used to temporarily store files that are larger
     * than the configured size threshold.
     *
     * @return The directory in which temporary files will be located.
     *
     * @see #setRepository(File)
     *
     */
    public File getRepository() {
        return repository;
    }

    /**
     * Sets the directory used to temporarily store files that are larger
     * than the configured size threshold.
     *
     * @param repository The directory in which temporary files will be located.
     *
     * @see #getRepository()
     *
     */
    public void setRepository(File repository) {
        this.repository = repository;
    }

    /**
     * Returns the size threshold beyond which files are written directly to
     * disk. The default value is 10240 bytes.
     *
     * @return The size threshold, in bytes.
     *
     * @see #setSizeThreshold(int)
     */
    public int getSizeThreshold() {
        return sizeThreshold;
    }

    /**
     * Sets the size threshold beyond which files are written directly to disk.
     *
     * @param sizeThreshold The size threshold, in bytes.
     *
     * @see #getSizeThreshold()
     *
     */
    public void setSizeThreshold(int sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Create a new {@link DiskFileItem}
     * instance from the supplied parameters and the local factory
     * configuration.
     *
     * @param fieldName   The name of the form field.
     * @param contentType The content type of the form field.
     * @param isFormField <code>true</code> if this is a plain form field;
     *                    <code>false</code> otherwise.
     * @param fileName    The name of the uploaded file, if any, as supplied
     *                    by the browser or other client.
     *
     * @return The newly created file item.
     */
    @Override
    public FileItem createItem(String fieldName, String contentType,
                               boolean isFormField, String fileName) {
        DiskFileItem result = new DiskFileItem(fieldName, contentType,
                isFormField, fileName, sizeThreshold, repository);
        result.setDefaultCharset(defaultCharset);
        FileCleaningTracker tracker = getFileCleaningTracker();
        if (tracker != null) {
            tracker.track(result.getTempFile(), result);
        }
        return result;
    }

    /**
     * Returns the tracker, which is responsible for deleting temporary
     * files.
     *
     * @return An instance of {@link FileCleaningTracker}, or null
     *   (default), if temporary files aren't tracked.
     */
    public FileCleaningTracker getFileCleaningTracker() {
        return fileCleaningTracker;
    }

    /**
     * Sets the tracker, which is responsible for deleting temporary
     * files.
     *
     * @param pTracker An instance of {@link FileCleaningTracker},
     *   which will from now on track the created files, or null
     *   (default), to disable tracking.
     */
    public void setFileCleaningTracker(FileCleaningTracker pTracker) {
        fileCleaningTracker = pTracker;
    }

    /**
     * Returns the default charset for use when no explicit charset
     * parameter is provided by the sender.
     * @return the default charset
     */
    public String getDefaultCharset() {
        return defaultCharset;
    }

    /**
     * Sets the default charset for use when no explicit charset
     * parameter is provided by the sender.
     * @param pCharset the default charset
     */
    public void setDefaultCharset(String pCharset) {
        defaultCharset = pCharset;
    }
}
