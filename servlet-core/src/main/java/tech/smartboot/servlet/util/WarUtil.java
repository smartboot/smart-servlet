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

package tech.smartboot.servlet.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/11/1
 */
public class WarUtil {
    /**
     * 解压War包
     *
     * @param warFile
     * @param destDirPath
     * @throws RuntimeException
     */
    public static void unZip(File warFile, File destDirPath) throws RuntimeException {
        long start = System.currentTimeMillis();
        try (ZipFile zipFile = new ZipFile(warFile)) {
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                File targetFile = new File(destDirPath, entry.getName());
                if (!targetFile.getParentFile().exists()) {
                    targetFile.getParentFile().mkdirs();
                }
                try (InputStream zipFileInputStream = zipFile.getInputStream(entry);
                     FileOutputStream zipFileOutputStream = new FileOutputStream(targetFile)) {
                    int len;

                    byte[] buf = new byte[1024];

                    while ((len = zipFileInputStream.read(buf)) != -1) {
                        zipFileOutputStream.write(buf, 0, len);
                    }
                }
            }

            long end = System.currentTimeMillis();

            System.out.println("解压[" + warFile.getName() + "]完成，耗时：" + (end - start) + " ms");

        } catch (Exception e) {
            throw new RuntimeException("unzip error from ZipUtils", e);
        }
    }
}
