/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.servlet.third.commons.io.input;

import java.io.IOException;

/**
 * The XmlStreamReaderException is thrown by the XmlStreamReader constructors if
 * the charset encoding can not be determined according to the XML 1.0
 * specification and RFC 3023.
 * <p>
 * The exception returns the unconsumed InputStream to allow the application to
 * do an alternate processing with the stream. Note that the original
 * InputStream given to the XmlStreamReader cannot be used as that one has been
 * already read.
 *
 * @version $Id: XmlStreamReaderException.java 1304052 2012-03-22 20:55:29Z ggregory $
 * @since 2.0
 */
public class XmlStreamReaderException extends IOException {

    private static final long serialVersionUID = 1L;

    private final String bomEncoding;

    private final String xmlGuessEncoding;

    private final String xmlEncoding;

    private final String contentTypeMime;

    private final String contentTypeEncoding;

    /**
     * Creates an exception instance if the charset encoding could not be
     * determined.
     * <p>
     * Instances of this exception are thrown by the XmlStreamReader.
     *
     * @param msg message describing the reason for the exception.
     * @param bomEnc BOM encoding.
     * @param xmlGuessEnc XML guess encoding.
     * @param xmlEnc XML prolog encoding.
     */
    public XmlStreamReaderException(String msg, String bomEnc,
            String xmlGuessEnc, String xmlEnc) {
        this(msg, null, null, bomEnc, xmlGuessEnc, xmlEnc);
    }

    /**
     * Creates an exception instance if the charset encoding could not be
     * determined.
     * <p>
     * Instances of this exception are thrown by the XmlStreamReader.
     *
     * @param msg message describing the reason for the exception.
     * @param ctMime MIME type in the content-type.
     * @param ctEnc encoding in the content-type.
     * @param bomEnc BOM encoding.
     * @param xmlGuessEnc XML guess encoding.
     * @param xmlEnc XML prolog encoding.
     */
    public XmlStreamReaderException(String msg, String ctMime, String ctEnc,
            String bomEnc, String xmlGuessEnc, String xmlEnc) {
        super(msg);
        contentTypeMime = ctMime;
        contentTypeEncoding = ctEnc;
        bomEncoding = bomEnc;
        xmlGuessEncoding = xmlGuessEnc;
        xmlEncoding = xmlEnc;
    }

    /**
     * Returns the BOM encoding found in the InputStream.
     *
     * @return the BOM encoding, null if none.
     */
    public String getBomEncoding() {
        return bomEncoding;
    }

    /**
     * Returns the encoding guess based on the first bytes of the InputStream.
     *
     * @return the encoding guess, null if it couldn't be guessed.
     */
    public String getXmlGuessEncoding() {
        return xmlGuessEncoding;
    }

    /**
     * Returns the encoding found in the XML prolog of the InputStream.
     *
     * @return the encoding of the XML prolog, null if none.
     */
    public String getXmlEncoding() {
        return xmlEncoding;
    }

    /**
     * Returns the MIME type in the content-type used to attempt determining the
     * encoding.
     *
     * @return the MIME type in the content-type, null if there was not
     *         content-type or the encoding detection did not involve HTTP.
     */
    public String getContentTypeMime() {
        return contentTypeMime;
    }

    /**
     * Returns the encoding in the content-type used to attempt determining the
     * encoding.
     *
     * @return the encoding in the content-type, null if there was not
     *         content-type, no encoding in it or the encoding detection did not
     *         involve HTTP.
     */
    public String getContentTypeEncoding() {
        return contentTypeEncoding;
    }
}
