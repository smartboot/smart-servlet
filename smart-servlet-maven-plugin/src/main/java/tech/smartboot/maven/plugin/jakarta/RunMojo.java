/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tech.smartboot.maven.plugin.jakarta;

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
@Execute(phase = LifecyclePhase.PACKAGE)
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
            Class<?> clazz = classLoader.loadClass("tech.smartboot.maven.plugin.jakarta.Starter");
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
