/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import tech.smartboot.servlet.Container;
import tech.smartboot.servlet.ServletContextRuntime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public abstract class Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(Plugin.class);
    /**
     * 是否已安装
     */
    private boolean installed;
    /**
     * 插件名称
     */
    private String pluginName;

    /**
     * 获取插件名称
     *
     * @return
     */
    public String pluginName() {
        if (pluginName == null) {
            pluginName = this.getClass().getSimpleName();
        }
        return pluginName;
    }


    /**
     * 安装插件,需要在servlet服务启动前调用
     */
    public final void install(Container container) {
        checkSate();
        initPlugin(container);
        installed = true;
    }

    /**
     * 初始化插件
     */
    protected void initPlugin(Container container) {
//        LOGGER.info("plugin:[" + pluginName() + "] do nothing when initPlugin!");
    }

    public void addServletContext(ServletContextRuntime runtime) {
//        LOGGER.info("plugin:[" + pluginName() + "] do nothing for container: " + runtime.getContextPath() + " when addServletContext!");
    }

    /**
     * servlet容器被启动成功之后被调用
     *
     * @param containerRuntime 当前启动成功的子容器
     */
    public void onContainerStartSuccess(ServletContextRuntime containerRuntime) {
//        LOGGER.info("plugin:[" + pluginName() + "] do nothing for container: " + containerRuntime.getContextPath() + " when start success!");
    }


    /**
     * servlet子容器启动前被调用
     *
     * @param containerRuntime 当前即将被启动的子容器
     */
    public void willStartContainer(ServletContextRuntime containerRuntime) {
//        LOGGER.info("plugin:[" + pluginName() + "] do nothing for container: " + containerRuntime.getContextPath() + " before start!");
    }

    /**
     * servlet子容器启动失败时被调用
     *
     * @param containerRuntime 当前启动失败的子容器
     */
    public void whenContainerStartError(ServletContextRuntime containerRuntime, Throwable throwable) {
//        LOGGER.info("plugin:[" + pluginName() + "] do nothing for container: " + containerRuntime.getContextPath() + " when start error!");
    }

    /**
     * 即将消耗子容器
     *
     * @param containerRuntime 即将被消耗的子容器
     */
    public void willStopContainer(ServletContextRuntime containerRuntime) {
//        LOGGER.info("plugin:[" + pluginName() + "]do nothing for container: " + containerRuntime.getContextPath() + " before stop!");
    }

    /**
     * 子容器已销毁
     *
     * @param containerRuntime 当前被消耗的子容器
     */
    public void onContainerStopped(ServletContextRuntime containerRuntime) {
//        LOGGER.info("plugin:[" + pluginName() + "] do nothing for container: " + containerRuntime.getContextPath() + " when stop!");
    }

    /**
     * 卸载插件,在容器服务停止前调用
     */
    public final void uninstall() {
        destroyPlugin();
    }

    protected InputStream getResource(String fileName) throws IOException {
        if (isSpringBoot()) {
            return getClass().getClassLoader().getResourceAsStream("smart-servlet/" + fileName);
        } else {
            File file = new File(getServletHome(), "conf/" + fileName);
            if (file.isFile()) {
                return Files.newInputStream(file.toPath());
            } else {
                return null;
            }
        }
    }

    protected boolean isSpringBoot() {
        return System.getProperty("smart-servlet-spring-boot-starter") != null;
    }

    protected File getServletHome() {
        String servletHome = System.getProperty("SERVLET_HOME");
        File servletFile = new File(new File(servletHome == null ? "" : servletHome).getAbsolutePath());
        if (!servletFile.isDirectory()) {
            System.out.println("SERVLET_HOME: " + servletFile.getAbsolutePath() + " is not exists!");
            System.exit(-1);
        }
        return servletFile;
    }

    /**
     * 销毁插件
     */
    protected void destroyPlugin() {
        LOGGER.info("plugin:[" + pluginName() + "] do nothing when destroyPlugin!");
    }

    private void checkSate() {
        if (installed) {
            throw new IllegalStateException("plugin [ " + pluginName() + " ] has installed!");
        }
    }
}
