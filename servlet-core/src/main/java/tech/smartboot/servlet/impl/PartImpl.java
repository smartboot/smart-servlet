/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.impl;

import jakarta.servlet.http.Part;
import tech.smartboot.servlet.third.commons.fileupload.ParameterParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/11/19
 */
public class PartImpl implements Part {

    private final org.smartboot.http.common.multipart.Part fileItem;
    private final File location;

    public PartImpl(org.smartboot.http.common.multipart.Part fileItem, File location) {
        this.fileItem = fileItem;
        this.location = location;
    }

    @Override
    public void delete() throws IOException {
        fileItem.delete();
    }

    @Override
    public String getContentType() {
        return fileItem.getContentType();
    }

    @Override
    public String getHeader(String name) {
        return fileItem.getHeader(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return fileItem.getHeaderNames();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return fileItem.getHeaders(name);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return fileItem.getInputStream();
    }

    @Override
    public String getName() {
        return fileItem.getName();
    }

    @Override
    public long getSize() {
        return fileItem.getSize();
    }

    @Override
    public void write(String fileName) throws IOException {
        fileItem.write(fileName);
    }

    public String getFormData() {
        return fileItem.getFormData();
    }

    /*
     * Adapted from FileUploadBase.getFileName()
     */
    @Override
    public String getSubmittedFileName() {
        String fileName = null;
        String cd = getHeader("Content-Disposition");
        if (cd != null) {
            String cdl = cd.toLowerCase(Locale.ENGLISH);
            if (cdl.startsWith("form-data") || cdl.startsWith("attachment")) {
                ParameterParser paramParser = new ParameterParser();
                paramParser.setLowerCaseNames(true);
                // Parameter parser can handle null input
                Map<String, String> params = paramParser.parse(cd, ';');
                if (params.containsKey("filename")) {
                    fileName = params.get("filename");
                    // The parser will remove surrounding '"' but will not
                    // unquote any \x sequences.
                    if (fileName != null) {
                        // RFC 6266. This is either a token or a quoted-string
                        if (fileName.indexOf('\\') > -1) {
                            // This is a quoted-string
                            fileName = unquote(fileName.trim());
                        } else {
                            // This is a token
                            fileName = fileName.trim();
                        }
                    } else {
                        // Even if there is no value, the parameter is present,
                        // so we return an empty file name rather than no file
                        // name.
                        fileName = "";
                    }
                }
            }
        }
        return fileName;
    }

    public static String unquote(String input) {
        if (input == null || input.length() < 2) {
            return input;
        }

        int start;
        int end;

        // Skip surrounding quotes if there are any
        if (input.charAt(0) == '"') {
            start = 1;
            end = input.length() - 1;
        } else {
            start = 0;
            end = input.length();
        }

        StringBuilder result = new StringBuilder();
        for (int i = start; i < end; i++) {
            char c = input.charAt(i);
            if (input.charAt(i) == '\\') {
                i++;
                result.append(input.charAt(i));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
