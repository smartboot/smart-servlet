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

import tech.smartboot.servlet.third.commons.io.FileUtils;
import tech.smartboot.servlet.third.commons.io.IOCase;
import tech.smartboot.servlet.third.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * FileAlterationObserver represents the state of files below a root directory,
 * checking the filesystem and notifying listeners of create, change or
 * delete events.
 * <p>
 * To use this implementation:
 * <ul>
 *   <li>Create {@link FileAlterationListener} implementation(s) that process
 *      the file/directory create, change and delete events</li>
 *   <li>Register the listener(s) with a {@link FileAlterationObserver} for
 *       the appropriate directory.</li>
 *   <li>Either register the observer(s) with a {@link FileAlterationMonitor} or
 *       run manually.</li>
 * </ul>
 *
 * <h2>Basic Usage</h2>
 * Create a {@link FileAlterationObserver} for the directory and register the listeners:
 * <pre>
 *      File directory = new File(new File("."), "src");
 *      FileAlterationObserver observer = new FileAlterationObserver(directory);
 *      observer.addListener(...);
 *      observer.addListener(...);
 * </pre>
 * To manually observe a directory, initialize the observer and invoked the
 * {@link #checkAndNotify()} method as required:
 * <pre>
 *      // intialize
 *      observer.init();
 *      ...
 *      // invoke as required
 *      observer.checkAndNotify();
 *      ...
 *      observer.checkAndNotify();
 *      ...
 *      // finished
 *      observer.finish();
 * </pre>
 * Alternatively, register the oberver(s) with a {@link FileAlterationMonitor},
 * which creates a new thread, invoking the observer at the specified interval:
 * <pre>
 *      long interval = ...
 *      FileAlterationMonitor monitor = new FileAlterationMonitor(interval);
 *      monitor.addObserver(observer);
 *      monitor.start();
 *      ...
 *      monitor.stop();
 * </pre>
 *
 * <h2>File Filters</h2>
 * This implementation can monitor portions of the file system
 * by using {@link FileFilter}s to observe only the files and/or directories
 * that are of interest. This makes it more efficient and reduces the
 * noise from <i>unwanted</i> file system events.
 * <p>
 * <a href="http://commons.apache.org/io/">Commons IO</a> has a good range of
 * useful, ready made 
 * <a href="../filefilter/package-summary.html">File Filter</a>
 * implementations for this purpose.
 * <p>
 * For example, to only observe 1) visible directories and 2) files with a ".java" suffix
 * in a root directory called "src" you could set up a {@link FileAlterationObserver} in the following
 * way:
 * <pre>
 *      // Create a FileFilter
 *      IOFileFilter directories = FileFilterUtils.and(
 *                                      FileFilterUtils.directoryFileFilter(),
 *                                      HiddenFileFilter.VISIBLE);
 *      IOFileFilter files       = FileFilterUtils.and(
 *                                      FileFilterUtils.fileFileFilter(),
 *                                      FileFilterUtils.suffixFileFilter(".java"));
 *      IOFileFilter filter = FileFilterUtils.or(directories, files);
 *
 *      // Create the File system observer and register File Listeners
 *      FileAlterationObserver observer = new FileAlterationObserver(new File("src"), filter);
 *      observer.addListener(...);
 *      observer.addListener(...);
 * </pre>
 *
 * <h2>FileEntry</h2>
 * {@link FileEntry} represents the state of a file or directory, capturing
 * {@link File} attributes at a point in time. Custom implementations of
 * {@link FileEntry} can be used to capture additional properties that the
 * basic implementation does not support. The {@link FileEntry#refresh(File)}
 * method is used to determine if a file or directory has changed since the last
 * check and stores the current state of the {@link File}'s properties.
 *
 * @see FileAlterationListener
 * @see FileAlterationMonitor
 * @version $Id: FileAlterationObserver.java 1304052 2012-03-22 20:55:29Z ggregory $
 * @since 2.0
 */
public class FileAlterationObserver implements Serializable {

    private final List<FileAlterationListener> listeners = new CopyOnWriteArrayList<FileAlterationListener>();
    private final FileEntry rootEntry;
    private final FileFilter fileFilter;
    private final Comparator<File> comparator;

    /**
     * Construct an observer for the specified directory.
     *
     * @param directoryName the name of the directory to observe
     */
    public FileAlterationObserver(String directoryName) {
        this(new File(directoryName));
    }

    /**
     * Construct an observer for the specified directory and file filter.
     *
     * @param directoryName the name of the directory to observe
     * @param fileFilter The file filter or null if none
     */
    public FileAlterationObserver(String directoryName, FileFilter fileFilter) {
        this(new File(directoryName), fileFilter);
    }

    /**
     * Construct an observer for the specified directory, file filter and
     * file comparator.
     *
     * @param directoryName the name of the directory to observe
     * @param fileFilter The file filter or null if none
     * @param caseSensitivity  what case sensitivity to use comparing file names, null means system sensitive
     */
    public FileAlterationObserver(String directoryName, FileFilter fileFilter, IOCase caseSensitivity) {
        this(new File(directoryName), fileFilter, caseSensitivity);
    }

    /**
     * Construct an observer for the specified directory.
     *
     * @param directory the directory to observe
     */
    public FileAlterationObserver(File directory) {
        this(directory, (FileFilter)null);
    }

    /**
     * Construct an observer for the specified directory and file filter.
     *
     * @param directory the directory to observe
     * @param fileFilter The file filter or null if none
     */
    public FileAlterationObserver(File directory, FileFilter fileFilter) {
        this(directory, fileFilter, (IOCase)null);
    }

    /**
     * Construct an observer for the specified directory, file filter and
     * file comparator.
     *
     * @param directory the directory to observe
     * @param fileFilter The file filter or null if none
     * @param caseSensitivity  what case sensitivity to use comparing file names, null means system sensitive
     */
    public FileAlterationObserver(File directory, FileFilter fileFilter, IOCase caseSensitivity) {
        this(new FileEntry(directory), fileFilter, caseSensitivity);
    }

    /**
     * Construct an observer for the specified directory, file filter and
     * file comparator.
     *
     * @param rootEntry the root directory to observe
     * @param fileFilter The file filter or null if none
     * @param caseSensitivity  what case sensitivity to use comparing file names, null means system sensitive
     */
    protected FileAlterationObserver(FileEntry rootEntry, FileFilter fileFilter, IOCase caseSensitivity) {
        if (rootEntry == null) {
            throw new IllegalArgumentException("Root entry is missing");
        }
        if (rootEntry.getFile() == null) {
            throw new IllegalArgumentException("Root directory is missing");
        }
        this.rootEntry = rootEntry;
        this.fileFilter = fileFilter;
        if (caseSensitivity == null || caseSensitivity.equals(IOCase.SYSTEM)) {
            this.comparator = NameFileComparator.NAME_SYSTEM_COMPARATOR;
        } else if (caseSensitivity.equals(IOCase.INSENSITIVE)) {
            this.comparator = NameFileComparator.NAME_INSENSITIVE_COMPARATOR;
        } else {
            this.comparator = NameFileComparator.NAME_COMPARATOR;
        }
    }

    /**
     * Return the directory being observed.
     *
     * @return the directory being observed
     */
    public File getDirectory() {
        return rootEntry.getFile();
    }

    /**
     * Return the fileFilter.
     *
     * @return the fileFilter
     * @since 2.1
     */
    public FileFilter getFileFilter() {
        return fileFilter;
    }

    /**
     * Add a file system listener.
     *
     * @param listener The file system listener
     */
    public void addListener(final FileAlterationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a file system listener.
     *
     * @param listener The file system listener
     */
    public void removeListener(final FileAlterationListener listener) {
        if (listener != null) {
            while (listeners.remove(listener)) {
            }
        }
    }

    /**
     * Returns the set of registered file system listeners.
     *
     * @return The file system listeners
     */
    public Iterable<FileAlterationListener> getListeners() {
        return listeners;
    }

    /**
     * Initialize the observer.
     *
     * @throws Exception if an error occurs
     */
    public void initialize() throws Exception {
        rootEntry.refresh(rootEntry.getFile());
        File[] files = listFiles(rootEntry.getFile());
        FileEntry[] children = files.length > 0 ? new FileEntry[files.length] : FileEntry.EMPTY_ENTRIES;
        for (int i = 0; i < files.length; i++) {
            children[i] = createFileEntry(rootEntry, files[i]);
        }
        rootEntry.setChildren(children);
    }

    /**
     * Final processing.
     *
     * @throws Exception if an error occurs
     */
    public void destroy() throws Exception {
    }

    /**
     * Check whether the file and its chlidren have been created, modified or deleted.
     */
    public void checkAndNotify() {

        /* fire onStart() */
        for (FileAlterationListener listener : listeners) {
            listener.onStart(this);
        }

        /* fire directory/file events */
        File rootFile = rootEntry.getFile();
        if (rootFile.exists()) {
            checkAndNotify(rootEntry, rootEntry.getChildren(), listFiles(rootFile));
        } else if (rootEntry.isExists()) {
            checkAndNotify(rootEntry, rootEntry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
        } else {
            // Didn't exist and still doesn't
        }

        /* fire onStop() */
        for (FileAlterationListener listener : listeners) {
            listener.onStop(this);
        }
    }

    /**
     * Compare two file lists for files which have been created, modified or deleted.
     *
     * @param parent The parent entry
     * @param previous The original list of files
     * @param files  The current list of files
     */
    private void checkAndNotify(FileEntry parent, FileEntry[] previous, File[] files) {
        int c = 0;
        FileEntry[] current = files.length > 0 ? new FileEntry[files.length] : FileEntry.EMPTY_ENTRIES;
        for (FileEntry entry : previous) {
            while (c < files.length && comparator.compare(entry.getFile(), files[c]) > 0) {
                current[c] = createFileEntry(parent, files[c]);
                doCreate(current[c]);
                c++;
            }
            if (c < files.length && comparator.compare(entry.getFile(), files[c]) == 0) {
                doMatch(entry, files[c]);
                checkAndNotify(entry, entry.getChildren(), listFiles(files[c]));
                current[c] = entry;
                c++;
            } else {
                checkAndNotify(entry, entry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
                doDelete(entry);
            }
        }
        for (; c < files.length; c++) {
            current[c] = createFileEntry(parent, files[c]);
            doCreate(current[c]);
        }
        parent.setChildren(current);
    }

    /**
     * Create a new file entry for the specified file.
     *
     * @param parent The parent file entry
     * @param file The file to create an entry for
     * @return A new file entry
     */
    private FileEntry createFileEntry(FileEntry parent, File file) {
        FileEntry entry = parent.newChildInstance(file);
        entry.refresh(file);
        File[] files = listFiles(file);
        FileEntry[] children = files.length > 0 ? new FileEntry[files.length] : FileEntry.EMPTY_ENTRIES;
        for (int i = 0; i < files.length; i++) {
            children[i] = createFileEntry(entry, files[i]);
        }
        entry.setChildren(children);
        return entry;
    }

    /**
     * Fire directory/file created events to the registered listeners.
     *
     * @param entry The file entry
     */
    private void doCreate(FileEntry entry) {
        for (FileAlterationListener listener : listeners) {
            if (entry.isDirectory()) {
                listener.onDirectoryCreate(entry.getFile());
            } else {
                listener.onFileCreate(entry.getFile());
            }
        }
        FileEntry[] children = entry.getChildren();
        for (FileEntry aChildren : children) {
            doCreate(aChildren);
        }
    }

    /**
     * Fire directory/file change events to the registered listeners.
     *
     * @param entry The previous file system entry
     * @param file The current file
     */
    private void doMatch(FileEntry entry, File file) {
        if (entry.refresh(file)) {
            for (FileAlterationListener listener : listeners) {
                if (entry.isDirectory()) {
                    listener.onDirectoryChange(file);
                } else {
                    listener.onFileChange(file);
                }
            }
        }
    }

    /**
     * Fire directory/file delete events to the registered listeners.
     *
     * @param entry The file entry
     */
    private void doDelete(FileEntry entry) {
        for (FileAlterationListener listener : listeners) {
            if (entry.isDirectory()) {
                listener.onDirectoryDelete(entry.getFile());
            } else {
                listener.onFileDelete(entry.getFile());
            }
        }
    }

    /**
     * List the contents of a directory
     *
     * @param file The file to list the contents of
     * @return the directory contents or a zero length array if
     * the empty or the file is not a directory
     */
    private File[] listFiles(File file) {
        File[] children = null;
        if (file.isDirectory()) {
            children = fileFilter == null ? file.listFiles() : file.listFiles(fileFilter);
        }
        if (children == null) {
            children = FileUtils.EMPTY_FILE_ARRAY;
        }
        if (comparator != null && children.length > 1) {
            Arrays.sort(children, comparator);
        }
        return children;
    }

    /**
     * Provide a String representation of this observer.
     *
     * @return a String representation of this observer
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("[file='");
        builder.append(getDirectory().getPath());
        builder.append('\'');
        if (fileFilter != null) {
            builder.append(", ");
            builder.append(fileFilter.toString());
        }
        builder.append(", listeners=");
        builder.append(listeners.size());
        builder.append("]");
        return builder.toString();
    }

}
