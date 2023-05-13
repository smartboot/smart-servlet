/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.testsuite;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.smartboot.servlet.ContainerRuntime;
import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.util.WarUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class ArquillianAppProvider {
    private static final Logger LOG = Logger.getLogger(ArquillianAppProvider.class.getName());

    /**
     * The prefix assigned to the temporary file where the archive is exported
     */
    private static final String EXPORT_FILE_PREFIX = "export";

    /**
     * Directory into which we'll extract export the war files
     */
    private static final File EXPORT_DIR;

    static {
        /*
         * Use of java.io.tmpdir Should be a last-resort fallback for temp directory.
         *
         * Use of java.io.tmpdir on CI systems is dangerous (overwrite possibility is extremely high)
         *
         * Use of java.io.tmpdir on Unix systems is unreliable (due to common /tmp dir cleanup processes)
         */
        File systemDefaultTmpDir = new File(System.getProperty("java.io.tmpdir"));

        // If running under maven + surefire, use information provided by surefire.
        String baseDirVal = System.getProperty("basedir");

        File mavenTmpDir = null;
        if (baseDirVal != null) {
            File baseDir = new File(baseDirVal);
            if (baseDir.exists() && baseDir.isDirectory()) {
                File targetDir = new File(baseDir, "target");
                if (targetDir.exists() && targetDir.isDirectory()) {
                    mavenTmpDir = new File(targetDir, "arquillian-jetty-temp");
                    mavenTmpDir.mkdirs();
                }
            }
        }

        if ((mavenTmpDir != null) && mavenTmpDir.exists() && mavenTmpDir.isDirectory()) {
            EXPORT_DIR = mavenTmpDir;
        } else {
            EXPORT_DIR = systemDefaultTmpDir;
        }

        // If the temp location doesn't exist or isn't a directory
        if (!EXPORT_DIR.exists() || !EXPORT_DIR.isDirectory()) {
            throw new IllegalStateException("Could not obtain export directory \"" + EXPORT_DIR.getAbsolutePath() + "\"");
        }
    }

    private final JettyEmbeddedConfiguration config;
    private ContainerRuntime deploymentManager;

    public ArquillianAppProvider(JettyEmbeddedConfiguration config) {
        this.config = config;
    }

    protected ServletContextRuntime createApp(ContainerRuntime containerRuntime, final Archive<?> archive) throws Exception {
        String name = archive.getName();
        int extOff = name.lastIndexOf('.');
        if (extOff <= 0) {
            throw new RuntimeException("Not a valid Web Archive filename: " + name);
        }
        String ext = name.substring(extOff).toLowerCase();
        if (!ext.equals(".war")) {
            throw new RuntimeException("Not a recognized Web Archive: " + name);
        }
        name = name.substring(0, extOff);

        final File exported;
        try {
            if (this.config.isUseArchiveNameAsContext()) {
                Path tmpDirectory = Files.createTempDirectory("arquillian-jetty");
                Path archivePath = tmpDirectory.resolveSibling(archive.getName());
                Files.deleteIfExists(archivePath);
                exported = Files.createFile(archivePath).toFile();
                exported.deleteOnExit();
            } else {
                // If this method returns successfully then it is guaranteed that:
                // 1. The file denoted by the returned abstract pathname did not exist before this method was invoked, and
                // 2. Neither this method nor any of its variants will return the same abstract pathname again in the current invocation of the virtual machine.
                exported = File.createTempFile(EXPORT_FILE_PREFIX, archive.getName(), EXPORT_DIR);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create temporary File in " + EXPORT_DIR + " to write exported archive",
                    e);
        }
        // We are overwriting the temporary file placeholder reserved by File#createTemplateFile()
        archive.as(ZipExporter.class).exportTo(exported, true);

        // Mark to delete when we come down
        // exported.deleteOnExit();

        // Add the context
        URI uri = exported.toURI();
        LOG.info("Webapp archive location: " + uri.toASCIIString());
        File dirFile = new File(exported.getParentFile(), name);
        System.out.println("开始解压[" + name + "]...");
        WarUtil.unZip(exported, dirFile);
        return containerRuntime.addRuntime(dirFile.getAbsolutePath(), "/" + name);
    }

}
