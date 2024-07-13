/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.jakarta.third.commons.io.monitor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;

/**
 * A runnable that spawns a monitoring thread triggering any
 * registered {@link FileAlterationObserver} at a specified interval.
 * 
 * @see FileAlterationObserver
 * @version $Id: FileAlterationMonitor.java 1304052 2012-03-22 20:55:29Z ggregory $
 * @since 2.0
 */
public final class FileAlterationMonitor implements Runnable {

    private final long interval;
    private final List<FileAlterationObserver> observers = new CopyOnWriteArrayList<FileAlterationObserver>();
    private Thread thread = null;
    private ThreadFactory threadFactory;
    private volatile boolean running = false;

    /**
     * Construct a monitor with a default interval of 10 seconds.
     */
    public FileAlterationMonitor() {
        this(10000);
    }

    /**
     * Construct a monitor with the specified interval.
     *
     * @param interval The amount of time in miliseconds to wait between
     * checks of the file system
     */
    public FileAlterationMonitor(long interval) {
        this.interval = interval;
    }

    /**
     * Construct a monitor with the specified interval and set of observers.
     *
     * @param interval The amount of time in miliseconds to wait between
     * checks of the file system
     * @param observers The set of observers to add to the monitor.
     */
    public FileAlterationMonitor(long interval, FileAlterationObserver... observers) {
        this(interval);
        if (observers != null) {
            for (FileAlterationObserver observer : observers) {
                addObserver(observer);
            }
        }
    }

    /**
     * Return the interval.
     *
     * @return the interval
     */
    public long getInterval() {
        return interval;
    }

    /**
     * Set the thread factory.
     *
     * @param threadFactory the thread factory
     */
    public synchronized void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    /**
     * Add a file system observer to this monitor.
     *
     * @param observer The file system observer to add
     */
    public void addObserver(final FileAlterationObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    /**
     * Remove a file system observer from this monitor.
     *
     * @param observer The file system observer to remove
     */
    public void removeObserver(final FileAlterationObserver observer) {
        if (observer != null) {
            while (observers.remove(observer)) {
            }
        }
    }

    /**
     * Returns the set of {@link FileAlterationObserver} registered with
     * this monitor. 
     *
     * @return The set of {@link FileAlterationObserver}
     */
    public Iterable<FileAlterationObserver> getObservers() {
        return observers;
    }

    /**
     * Start monitoring.
     *
     * @throws Exception if an error occurs initializing the observer
     */
    public synchronized void start() throws Exception {
        if (running) {
            throw new IllegalStateException("Monitor is already running");
        }
        for (FileAlterationObserver observer : observers) {
            observer.initialize();
        }
        running = true;
        if (threadFactory != null) {
            thread = threadFactory.newThread(this);
        } else {
            thread = new Thread(this);
        }
        thread.start();
    }

    /**
     * Stop monitoring.
     *
     * @throws Exception if an error occurs initializing the observer
     */
    public synchronized void stop() throws Exception {
        stop(interval);
    }

    /**
     * Stop monitoring.
     *
     * @param stopInterval the amount of time in milliseconds to wait for the thread to finish.
     * A value of zero will wait until the thread is finished (see {@link Thread#join(long)}).
     * @throws Exception if an error occurs initializing the observer
     * @since 2.1
     */
    public synchronized void stop(long stopInterval) throws Exception {
        if (running == false) {
            throw new IllegalStateException("Monitor is not running");
        }
        running = false;
        try {
            thread.join(stopInterval);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        for (FileAlterationObserver observer : observers) {
            observer.destroy();
        }
    }

    /**
     * Run.
     */
    public void run() {
        while (running) {
            for (FileAlterationObserver observer : observers) {
                observer.checkAndNotify();
            }
            if (!running) {
                break;
            }
            try {
                Thread.sleep(interval);
            } catch (final InterruptedException ignored) {
            }
        }
    }
}
