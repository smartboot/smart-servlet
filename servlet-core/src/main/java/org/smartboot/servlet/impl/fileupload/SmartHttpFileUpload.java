/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: SmartHttpFileUpload.java
 * Date: 2020-04-03
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.servlet.impl.fileupload;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.smartboot.http.server.HttpRequest;

import java.io.IOException;


public class SmartHttpFileUpload extends FileUpload {

    public FileItemIterator getItemIterator(HttpRequest request)
            throws FileUploadException, IOException {
        return super.getItemIterator(new SmartHttpRequestContext(request));
    }
}
