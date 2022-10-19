/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: RunMojo.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.maven.plugin.servlet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/4
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
@Execute(phase = LifecyclePhase.PROCESS_CLASSES)
public class RunMojo extends AbstractMojo {
    /**
     * @since 1.0
     */
    @Parameter(defaultValue = "${plugin.artifacts}", required = true)
    private List<Artifact> pluginArtifacts;

    /**
     * 编译后的存放目录
     */
    @Parameter(defaultValue = "${project.build.directory}/")
    private File configurationDir;

    @Parameter(defaultValue = "${project.packaging}", required = true, readonly = true)
    private String packaging;

    @Parameter(defaultValue = "${project.artifactId}", required = true, readonly = true)
    private String artifactId;

    @Parameter(defaultValue = "${project.version}", required = true, readonly = true)
    private String version;

    @Parameter(defaultValue = "8080")
    private int port;

    @Parameter(defaultValue = "/")
    private String path;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        long startTime = System.currentTimeMillis();
        try {
            List<URL> urlList = new ArrayList<>();
            pluginArtifacts.forEach(artifact -> {
                try {
                    System.out.println("plugin: " + artifact.getFile().getAbsolutePath());
                    urlList.add(artifact.getFile().toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            });
            URL[] urls = new URL[urlList.size()];
            urlList.toArray(urls);
            URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
            File webFile = new File(configurationDir, artifactId + "-" + version);
            Class<?> clazz = classLoader.loadClass("org.smartboot.maven.plugin.servlet.Starter");
            clazz.getConstructor(String.class, String.class, int.class, ClassLoader.class).newInstance(webFile.getAbsolutePath(), path, port, classLoader);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
        getLog().info("smart-servlet start success, cost: " + (System.currentTimeMillis() - startTime));
        Object lock = new Object();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
