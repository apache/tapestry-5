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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPOutputStream;

public class StreamableResourceImpl implements StreamableResource
{
    private final URL url;

    private final long lastModified;

    private boolean uncompressedCached;

    private int size;

    private String contentType;

    private boolean compressedCached;

    private int compressedSize;

    // TODO: For the moment, we keep the compressed stream in memory. A later solution, for larger files,
    // may want to store the compressed version externally, as a temporary file.
    private byte[] compressedBytes;

    public StreamableResourceImpl(URL url, long lastModified)
    {
        this.url = url;
        this.lastModified = lastModified;
    }

    public int getSize(boolean compress) throws IOException
    {
        return compress ? getCompressedSize() : getUncompressedSize();
    }

    public InputStream getStream(boolean compress) throws IOException
    {
        return compress ? getCompressedStream() : getUncompressedStream();
    }

    private synchronized int getCompressedSize() throws IOException
    {
        updateCompressedCachedValues();

        return compressedSize;
    }

    private synchronized InputStream getCompressedStream() throws IOException
    {
        updateCompressedCachedValues();

        return new ByteArrayInputStream(compressedBytes);
    }

    private synchronized int getUncompressedSize() throws IOException
    {
        updateUncompressedCachedValues();

        return size;
    }

    public long getLastModified() throws IOException
    {
        return lastModified;
    }

    public synchronized String getContentType() throws IOException
    {
        updateUncompressedCachedValues();

        return contentType;
    }

    private void updateUncompressedCachedValues() throws IOException
    {
        if (uncompressedCached) return;

        URLConnection connection = url.openConnection();

        size = connection.getContentLength();
        contentType = connection.getContentType();

        uncompressedCached = true;
    }

    private void updateCompressedCachedValues() throws IOException
    {
        if (compressedCached) return;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream compressor = new BufferedOutputStream(new GZIPOutputStream(bos));

        InputStream is = null;

        try
        {
            is = getUncompressedStream();

            TapestryInternalUtils.copy(is, compressor);

            compressor.close();
        }
        finally
        {
            InternalUtils.close(is);
        }

        compressedBytes = bos.toByteArray();
        compressedSize = bos.size();

        compressedCached = true;
    }

    private InputStream getUncompressedStream() throws IOException
    {
        return new BufferedInputStream(url.openStream());
    }
}
