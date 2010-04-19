// Copyright 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services.assets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.ResourceCache;
import org.apache.tapestry5.internal.services.StreamableResource;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.services.assets.AssetRequestHandler;
import org.apache.tapestry5.services.javascript.JavascriptStack;
import org.apache.tapestry5.services.javascript.JavascriptStackSource;

public class StackAssetRequestHandler implements AssetRequestHandler, InvalidationListener
{
    private final ResourceCache resourceCache;

    private final JavascriptStackSource javascriptStackSource;

    private final LocalizationSetter localizationSetter;

    private final ResponseCompressionAnalyzer compressionAnalyzer;

    private final boolean productionMode;

    private final Pattern pathPattern = Pattern.compile("^(.+)/(.+)\\.js$");

    // Two caches, keyed on extra path. Both are accessed only from synchronized blocks.
    private final Map<String, ByteArrayOutputStream> uncompressedCache = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, ByteArrayOutputStream> compressedCache = CollectionFactory.newCaseInsensitiveMap();

    public StackAssetRequestHandler(ResourceCache resourceCache, JavascriptStackSource javascriptStackSource,
            LocalizationSetter localizationSetter, ResponseCompressionAnalyzer compressionAnalyzer,

            @Symbol(SymbolConstants.PRODUCTION_MODE)
            boolean productionMode)
    {
        this.resourceCache = resourceCache;
        this.javascriptStackSource = javascriptStackSource;
        this.localizationSetter = localizationSetter;
        this.compressionAnalyzer = compressionAnalyzer;
        this.productionMode = productionMode;
    }

    public boolean handleAssetRequest(Request request, Response response, String extraPath) throws IOException
    {
        boolean compress = compressionAnalyzer.isGZipSupported();

        ByteArrayOutputStream stream = getStream(extraPath, compress);

        // The whole point of this is to force the client to aggressively cache the combined, virtual
        // stack asset.

        long lastModified = System.currentTimeMillis();
        response.setDateHeader("Last-Modified", lastModified);

        if (productionMode)
            response.setDateHeader("Expires", lastModified + InternalConstants.TEN_YEARS);

        response.setContentLength(stream.size());

        // Inform the upper layers that we are controlled compression here.
        request.setAttribute(InternalConstants.SUPPRESS_COMPRESSION, true);

        if (compress)
            response.setHeader(InternalConstants.CONTENT_ENCODING_HEADER, InternalConstants.GZIP_CONTENT_ENCODING);

        // CSS support is problematic, because of relative URLs inside the CSS files. For the
        // moment, only JavaScript is supported.

        OutputStream output = response.getOutputStream("text/javascript");

        stream.writeTo(output);

        output.close();

        return true;
    }

    /** Notified by the {@link ResourceCache} when resource files change; the internal caches are cleared. */
    public synchronized void objectWasInvalidated()
    {
        uncompressedCache.clear();
        compressedCache.clear();
    }

    private ByteArrayOutputStream getStream(String extraPath, boolean compressed) throws IOException
    {
        return compressed ? getCompressedStream(extraPath) : getUncompressedStream(extraPath);
    }

    private synchronized ByteArrayOutputStream getCompressedStream(String extraPath) throws IOException
    {
        ByteArrayOutputStream result = compressedCache.get(extraPath);

        if (result == null)
        {
            ByteArrayOutputStream uncompressed = getUncompressedStream(extraPath);
            result = compressStream(uncompressed);
            compressedCache.put(extraPath, result);
        }

        return result;
    }

    private synchronized ByteArrayOutputStream getUncompressedStream(String extraPath) throws IOException
    {
        ByteArrayOutputStream result = uncompressedCache.get(extraPath);

        if (result == null)
        {
            result = assembleStackContent(extraPath);
            uncompressedCache.put(extraPath, result);
        }

        return result;
    }

    private ByteArrayOutputStream assembleStackContent(String extraPath) throws IOException
    {
        Matcher matcher = pathPattern.matcher(extraPath);

        if (!matcher.matches())
            throw new RuntimeException("Invalid path for a stack asset request.");

        String localeName = matcher.group(1);
        String stackName = matcher.group(2);

        return assembleStackContent(localeName, stackName);
    }

    private ByteArrayOutputStream assembleStackContent(String localeName, String stackName) throws IOException
    {
        localizationSetter.setNonPeristentLocaleFromLocaleName(localeName);

        JavascriptStack stack = javascriptStackSource.getStack(stackName);
        List<Asset> libraries = stack.getJavascriptLibraries();

        return assembleStackContent(libraries);
    }

    private ByteArrayOutputStream assembleStackContent(List<Asset> libraries) throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(result, "UTF-8");
        PrintWriter writer = new PrintWriter(osw, true);

        JSONArray paths = new JSONArray();

        for (Asset library : libraries)
        {
            String path = library.toClientURL();

            paths.put(path);

            writer.format("\n/* %s */;\n", path);

            streamLibraryContent(library, result);
        }

        writer.format("\n;/**/\nTapestry.markScriptLibrariesLoaded(%s);\n", paths);

        writer.close();

        return result;
    }

    private void streamLibraryContent(Asset library, OutputStream outputStream) throws IOException
    {
        Resource resource = library.getResource();

        StreamableResource streamable = resourceCache.getStreamableResource(resource);

        InputStream inputStream = streamable.getStream(false);

        TapestryInternalUtils.copy(inputStream, outputStream);
    }

    private ByteArrayOutputStream compressStream(ByteArrayOutputStream uncompressed) throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        OutputStream compressor = new GZIPOutputStream(result);

        uncompressed.writeTo(compressor);

        compressor.close();

        return result;
    }

}
