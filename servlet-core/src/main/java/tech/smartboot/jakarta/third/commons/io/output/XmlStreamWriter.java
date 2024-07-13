/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.jakarta.third.commons.io.output;

import tech.smartboot.jakarta.third.commons.io.input.XmlStreamReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Character stream that handles all the necessary Voodo to figure out the
 * charset encoding of the XML document written to the stream.
 *
 * @version $Id: XmlStreamWriter.java 1304052 2012-03-22 20:55:29Z ggregory $
 * @see XmlStreamReader
 * @since 2.0
 */
public class XmlStreamWriter extends Writer {
    private static final int BUFFER_SIZE = 4096;

    private final OutputStream out;

    private final String defaultEncoding;

    private StringWriter xmlPrologWriter = new StringWriter(BUFFER_SIZE);

    private Writer writer;

    private String encoding;

    /**
     * Construct an new XML stream writer for the specified output stream
     * with a default encoding of UTF-8.
     *
     * @param out The output stream
     */
    public XmlStreamWriter(OutputStream out) {
        this(out, null);
    }

    /**
     * Construct an new XML stream writer for the specified output stream
     * with the specified default encoding.
     *
     * @param out The output stream
     * @param defaultEncoding The default encoding if not encoding could be detected
     */
    public XmlStreamWriter(OutputStream out, String defaultEncoding) {
        this.out = out;
        this.defaultEncoding = defaultEncoding != null ? defaultEncoding : "UTF-8";
    }

    /**
     * Construct an new XML stream writer for the specified file
     * with a default encoding of UTF-8.
     * 
     * @param file The file to write to
     * @throws FileNotFoundException if there is an error creating or
     * opening the file
     */
    public XmlStreamWriter(File file) throws FileNotFoundException {
        this(file, null);
    }

    /**
     * Construct an new XML stream writer for the specified file
     * with the specified default encoding.
     * 
     * @param file The file to write to
     * @param defaultEncoding The default encoding if not encoding could be detected
     * @throws FileNotFoundException if there is an error creating or
     * opening the file
     */
    public XmlStreamWriter(File file, String defaultEncoding) throws FileNotFoundException {
        this(new FileOutputStream(file), defaultEncoding);
    }

    /**
     * Return the detected encoding.
     *
     * @return the detected encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Return the default encoding.
     *
     * @return the default encoding
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Close the underlying writer.
     *
     * @throws IOException if an error occurs closing the underlying writer
     */
    @Override
    public void close() throws IOException {
        if (writer == null) {
            encoding = defaultEncoding;
            writer = new OutputStreamWriter(out, encoding);
            writer.write(xmlPrologWriter.toString());
        }
        writer.close();
    }

    /**
     * Flush the underlying writer.
     *
     * @throws IOException if an error occurs flushing the underlying writer
     */
    @Override
    public void flush() throws IOException {
        if (writer != null) {
            writer.flush();
        }
    }

    /**
     * Detect the encoding.
     *
     * @param cbuf the buffer to write the characters from
     * @param off The start offset
     * @param len The number of characters to write
     * @throws IOException if an error occurs detecting the encoding
     */
    private void detectEncoding(char[] cbuf, int off, int len)
            throws IOException {
        int size = len;
        StringBuffer xmlProlog = xmlPrologWriter.getBuffer();
        if (xmlProlog.length() + len > BUFFER_SIZE) {
            size = BUFFER_SIZE - xmlProlog.length();
        }
        xmlPrologWriter.write(cbuf, off, size);

        // try to determine encoding
        if (xmlProlog.length() >= 5) {
            if (xmlProlog.substring(0, 5).equals("<?xml")) {
                // try to extract encoding from XML prolog
                int xmlPrologEnd = xmlProlog.indexOf("?>");
                if (xmlPrologEnd > 0) {
                    // ok, full XML prolog written: let's extract encoding
                    Matcher m = ENCODING_PATTERN.matcher(xmlProlog.substring(0,
                            xmlPrologEnd));
                    if (m.find()) {
                        encoding = m.group(1).toUpperCase();
                        encoding = encoding.substring(1, encoding.length() - 1);
                    } else {
                        // no encoding found in XML prolog: using default
                        // encoding
                        encoding = defaultEncoding;
                    }
                } else {
                    if (xmlProlog.length() >= BUFFER_SIZE) {
                        // no encoding found in first characters: using default
                        // encoding
                        encoding = defaultEncoding;
                    }
                }
            } else {
                // no XML prolog: using default encoding
                encoding = defaultEncoding;
            }
            if (encoding != null) {
                // encoding has been chosen: let's do it
                xmlPrologWriter = null;
                writer = new OutputStreamWriter(out, encoding);
                writer.write(xmlProlog.toString());
                if (len > size) {
                    writer.write(cbuf, off + size, len - size);
                }
            }
        }
    }

    /**
     * Write the characters to the underlying writer, detecing encoding.
     * 
     * @param cbuf the buffer to write the characters from
     * @param off The start offset
     * @param len The number of characters to write
     * @throws IOException if an error occurs detecting the encoding
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (xmlPrologWriter != null) {
            detectEncoding(cbuf, off, len);
        } else {
            writer.write(cbuf, off, len);
        }
    }

    static final Pattern ENCODING_PATTERN = XmlStreamReader.ENCODING_PATTERN;
}
