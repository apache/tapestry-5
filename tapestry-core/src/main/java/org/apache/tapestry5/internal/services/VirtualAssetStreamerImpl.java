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

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.*;

import java.io.*;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * This implementation uses {@link org.apache.tapestry5.internal.services.ResourceCache}, but is also a listener of
 * invalidation events from ResourceCache. When a Asset resource changes, any cached data in this service is discarded.
 *
 * @since 5.1.0.2
 */
public class VirtualAssetStreamerImpl implements VirtualAssetStreamer, InvalidationListener
{
    private final ResourceCache resourceCache;

    private final ResponseCompressionAnalyzer compressionAnalyzer;

    private final ClientDataEncoder clientDataEncoder;

    private final AssetResourceLocator resourceLocator;

    private final Request request;

    private final Response response;

    /**
     * Cache keyed on client data (encoding the Asset paths to combine), value is the assembled virtual asset.
     */
    private final Map<String, ByteArrayOutputStream> cache = CollectionFactory.newConcurrentMap();

    /**
     * Cached keyed on client data ... value is the GZIP compressed value for the virtual asset.
     */
    private final Map<String, ByteArrayOutputStream> compressedCache = CollectionFactory.newConcurrentMap();

    public VirtualAssetStreamerImpl(ResourceCache resourceCache, ResponseCompressionAnalyzer compressionAnalyzer,
                                    ClientDataEncoder clientDataEncoder, AssetResourceLocator resourceLocator,
                                    Request request, Response response)
    {
        this.resourceCache = resourceCache;
        this.compressionAnalyzer = compressionAnalyzer;
        this.clientDataEncoder = clientDataEncoder;
        this.resourceLocator = resourceLocator;
        this.request = request;
        this.response = response;
    }

    public void streamVirtualAsset(String clientData) throws IOException
    {
        boolean compress = compressionAnalyzer.isGZipSupported();

        ByteArrayOutputStream stream = getVirtualStream(clientData, compress);

        // The whole point of this is to force the client to aggresively cache the combined, virtual
        // asset.

        long lastModified = System.currentTimeMillis();

        response.setDateHeader("Last-Modified", lastModified);
        response.setDateHeader("Expires", lastModified + InternalConstants.TEN_YEARS);

        response.setContentLength(stream.size());

        if (compress)
            response.setHeader(InternalConstants.CONTENT_ENCODING_HEADER, InternalConstants.GZIP_CONTENT_ENCODING);

        request.setAttribute(InternalConstants.SUPPRESS_COMPRESSION, true);

        // CSS support is problematic, because of relative URLs inside the CSS files. For the moment, only
        // JavaScript is supported.

        OutputStream output = response.getOutputStream("text/javascript");

        stream.writeTo(output);

        output.close();
    }

    private ByteArrayOutputStream getVirtualStream(String clientData, boolean compress) throws IOException
    {
        return compress ? getCompressedVirtualStream(clientData) : getVirtualStream(clientData);
    }

    private ByteArrayOutputStream getCompressedVirtualStream(String clientData) throws IOException
    {
        ByteArrayOutputStream result = compressedCache.get(clientData);

        if (result == null)
        {
            ByteArrayOutputStream virtualStream = getVirtualStream(clientData);

            result = compressStream(virtualStream);

            compressedCache.put(clientData, result);
        }

        return result;
    }

    private ByteArrayOutputStream compressStream(ByteArrayOutputStream stream) throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        GZIPOutputStream gzip = new GZIPOutputStream(result);

        stream.writeTo(gzip);

        gzip.close();

        return result;
    }

    /**
     * This is where it gets tricky. We need to take the clientData and turn it back into a bytestream, then read the
     * UTF strings for the paths out of it and add those all together.
     *
     * @param clientData
     * @return
     */
    private ByteArrayOutputStream getVirtualStream(String clientData) throws IOException
    {
        ByteArrayOutputStream result = cache.get(clientData);

        if (result == null)
        {
            result = constructVirtualAssetStream(clientData);

            cache.put(clientData, result);
        }

        return result;
    }

    private ByteArrayOutputStream constructVirtualAssetStream(String clientData)
            throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        ObjectInputStream inputStream = clientDataEncoder.decodeEncodedClientData(clientData);

        OutputStreamWriter osw = new OutputStreamWriter(result, "UTF-8");
        PrintWriter writer = new PrintWriter(osw, true);

        JSONArray paths = new JSONArray();

        int count = inputStream.readInt();

        for (int i = 0; i < count; i++)
        {
            String path = inputStream.readUTF();

            // Make sure the final statement in the previous file was terminated; likewise
            // any unterminated comment. Ugly, but necessary.

            writer.format("\n/* %s */;\n", path);

            streamPath(path, result);

            // Add the context path prefix to the path (which is within the context) so that the
            // loaded script URL can be properly tracked on the client side.

            paths.put(request.getContextPath() + path);

        }

        // Need to add some text to the result.

        writer.format("\n;/**/\nTapestry.markScriptLibrariesLoaded(%s);\n", paths);

        return result;
    }

    private void streamPath(String path, ByteArrayOutputStream output) throws IOException
    {
        Resource resource = resourceLocator.findResourceForPath(path);

        StreamableResource streamable = resourceCache.getStreamableResource(resource);

        InputStream inputStream = streamable.getStream(false);

        TapestryInternalUtils.copy(inputStream, output);
    }

    public void objectWasInvalidated()
    {
        cache.clear();
        compressedCache.clear();
    }
}
