/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.jakarta.third.commons.io;

/**
 * Monitors a thread, interrupting it of it reaches the specified timout.
 * <p>
 * This works by sleeping until the specified timout amount and then
 * interrupting the thread being monitored. If the thread being monitored
 * completes its work before being interrupted, it should <code>interrupt()<code>
 * the <i>monitor</i> thread.
 * <p>
 * 
 * <pre>
 *       long timeoutInMillis = 1000;
 *       try {
 *           Thread monitor = ThreadMonitor.start(timeoutInMillis);
 *           // do some work here
 *           ThreadMonitor.stop(monitor);
 *       } catch (InterruptedException e) {
 *           // timed amount was reached
 *       }
 * </pre>
 *
 * @version  $Id: ThreadMonitor.java 1302056 2012-03-18 03:03:38Z ggregory $
 */
class ThreadMonitor implements Runnable {

    private final Thread thread;
    private final long timeout;

    /**
     * Start monitoring the current thread.
     *
     * @param timeout The timout amount in milliseconds
     * or no timeout if the value is zero or less
     * @return The monitor thread or <code>null</code>
     * if the timout amount is not greater than zero
     */
    public static Thread start(long timeout) {
        return start(Thread.currentThread(), timeout);
    }

    /**
     * Start monitoring the specified thread.
     *
     * @param thread The thread The thread to monitor
     * @param timeout The timout amount in milliseconds
     * or no timeout if the value is zero or less
     * @return The monitor thread or <code>null</code>
     * if the timout amount is not greater than zero
     */
    public static Thread start(Thread thread, long timeout) {
        Thread monitor = null;
        if (timeout > 0) {
            ThreadMonitor timout = new ThreadMonitor(thread, timeout);
            monitor = new Thread(timout, ThreadMonitor.class.getSimpleName());
            monitor.setDaemon(true);
            monitor.start();
        }
        return monitor;
    }

    /**
     * Stop monitoring the specified thread.
     *
     * @param thread The monitor thread, may be <code>null</code>
     */
    public static void stop(Thread thread) {
        if (thread != null) {
            thread.interrupt();
        }
    }

    /**
     * Construct and new monitor.
     *
     * @param thread The thread to monitor
     * @param timeout The timout amount in milliseconds
     */
    private ThreadMonitor(Thread thread, long timeout) {
        this.thread = thread;
        this.timeout = timeout;
    }

    /**
     * Sleep until the specified timout amount and then
     * interrupt the thread being monitored.
     *
     * @see Runnable#run()
     */
    public void run() {
        try {
            Thread.sleep(timeout);
            thread.interrupt();
        } catch (InterruptedException e) {
            // timeout not reached
        }
    }
}