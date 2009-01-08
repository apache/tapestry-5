// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.gzip;

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.services.ResponseCompressionAnalyzer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A buffered output stream that, when a certain number of bytes is buffered (the cutover point) will open a compressed
 * stream (via {@link org.apache.tapestry5.services.Response#getOutputStream(String)}
 */
public class BufferedGZipOutputStream extends ServletOutputStream
{
    private final String contentType;

    private final HttpServletResponse response;

    private final ResponseCompressionAnalyzer analyzer;

    private final int cutover;

    private ByteArrayOutputStream byteArrayOutputStream;

    /**
     * Initially the ByteArrayOutputStream, later the response output stream (possibly wrapped with a
     * GZIPOutputStream).
     */
    private OutputStream currentOutputStream;

    public BufferedGZipOutputStream(String contentType, HttpServletResponse response, int cutover,
                                    ResponseCompressionAnalyzer analyzer)
    {
        this.contentType = contentType;
        this.response = response;
        this.cutover = cutover;
        this.analyzer = analyzer;

        byteArrayOutputStream = new ByteArrayOutputStream(cutover);

        currentOutputStream = byteArrayOutputStream;
    }

    private void checkForCutover() throws IOException
    {
        if (byteArrayOutputStream == null) return;

        if (byteArrayOutputStream.size() < cutover) return;

        // Time to switch over to GZIP.
        openResponseOutputStream(true);
    }

    private void openResponseOutputStream(boolean gzip) throws IOException
    {
        OutputStream responseOutputStream = response.getOutputStream();

        boolean useCompression = gzip && analyzer.isCompressable(contentType);

        OutputStream possiblyCompressed = useCompression
                                          ? new GZIPOutputStream(responseOutputStream)
                                          : responseOutputStream;

        if (useCompression)
            response.setHeader(InternalConstants.CONTENT_ENCODING_HEADER, InternalConstants.GZIP_CONTENT_ENCODING);

        currentOutputStream =
                new BufferedOutputStream(possiblyCompressed);

        // Write what content we already have to the new stream.

        byteArrayOutputStream.writeTo(currentOutputStream);

        byteArrayOutputStream = null;
    }

    public void write(int b) throws IOException
    {
        currentOutputStream.write(b);

        checkForCutover();
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        currentOutputStream.write(b);

        checkForCutover();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        currentOutputStream.write(b, off, len);

        checkForCutover();
    }

    @Override
    public void flush() throws IOException
    {
        forceOutputStream().flush();
    }

    @Override
    public void close() throws IOException
    {
        // When closing, if we haven't accumulated enough output yet to start compressing,
        // then send what we have, uncompressed.

        forceOutputStream().close();
    }

    private OutputStream forceOutputStream() throws IOException
    {
        if (byteArrayOutputStream != null)
            openResponseOutputStream(false);

        return currentOutputStream;
    }
}
