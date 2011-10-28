// Copyright 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.IOOperation;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.services.assets.*;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.JavaScriptStackSource;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

public class StackAssetRequestHandler implements AssetRequestHandler, InvalidationListener
{
    private static final String JAVASCRIPT_CONTENT_TYPE = "text/javascript";

    private final StreamableResourceSource streamableResourceSource;

    private final JavaScriptStackSource javascriptStackSource;

    private final LocalizationSetter localizationSetter;

    private final ResponseCompressionAnalyzer compressionAnalyzer;

    private final ResourceStreamer resourceStreamer;

    private final Pattern pathPattern = Pattern.compile("^(.+)/(.+)\\.js$");

    // Two caches, keyed on extra path. Both are accessed only from synchronized blocks.
    private final Map<String, StreamableResource> uncompressedCache = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, StreamableResource> compressedCache = CollectionFactory.newCaseInsensitiveMap();

    private final ResourceMinimizer resourceMinimizer;

    private final OperationTracker tracker;

    private final boolean minificationEnabled;

    private final ResourceChangeTracker resourceChangeTracker;

    public StackAssetRequestHandler(StreamableResourceSource streamableResourceSource,
                                    JavaScriptStackSource javascriptStackSource, LocalizationSetter localizationSetter,
                                    ResponseCompressionAnalyzer compressionAnalyzer, ResourceStreamer resourceStreamer,
                                    ResourceMinimizer resourceMinimizer, OperationTracker tracker,

                                    @Symbol(SymbolConstants.MINIFICATION_ENABLED)
                                    boolean minificationEnabled, ResourceChangeTracker resourceChangeTracker)
    {
        this.streamableResourceSource = streamableResourceSource;
        this.javascriptStackSource = javascriptStackSource;
        this.localizationSetter = localizationSetter;
        this.compressionAnalyzer = compressionAnalyzer;
        this.resourceStreamer = resourceStreamer;
        this.resourceMinimizer = resourceMinimizer;
        this.tracker = tracker;
        this.minificationEnabled = minificationEnabled;
        this.resourceChangeTracker = resourceChangeTracker;
    }

    @PostInjection
    public void listenToInvalidations(ResourceChangeTracker resourceChangeTracker)
    {
        resourceChangeTracker.addInvalidationListener(this);
    }

    public boolean handleAssetRequest(Request request, Response response, final String extraPath) throws IOException
    {
        TapestryInternalUtils.performIO(tracker, String.format("Streaming asset stack %s", extraPath),
                new IOOperation()
                {
                    public void perform() throws IOException
                    {
                        boolean compress = compressionAnalyzer.isGZipSupported();

                        StreamableResource resource = getResource(extraPath, compress);

                        resourceStreamer.streamResource(resource);
                    }
                });

        return true;
    }

    /**
     * Notified by the {@link ResourceChangeTracker} when (any) resource files change; the internal caches are cleared.
     */
    public synchronized void objectWasInvalidated()
    {
        uncompressedCache.clear();
        compressedCache.clear();
    }

    private StreamableResource getResource(String extraPath, boolean compressed) throws IOException
    {
        return compressed ? getCompressedResource(extraPath) : getUncompressedResource(extraPath);
    }

    private synchronized StreamableResource getCompressedResource(String extraPath) throws IOException
    {
        StreamableResource result = compressedCache.get(extraPath);

        if (result == null)
        {
            StreamableResource uncompressed = getUncompressedResource(extraPath);
            result = compressStream(uncompressed);
            compressedCache.put(extraPath, result);
        }

        return result;
    }

    private synchronized StreamableResource getUncompressedResource(String extraPath) throws IOException
    {
        StreamableResource result = uncompressedCache.get(extraPath);

        if (result == null)
        {
            result = assembleStackContent(extraPath);
            uncompressedCache.put(extraPath, result);
        }

        return result;
    }

    private StreamableResource assembleStackContent(String extraPath) throws IOException
    {
        Matcher matcher = pathPattern.matcher(extraPath);

        if (!matcher.matches())
            throw new RuntimeException("Invalid path for a stack asset request.");

        String localeName = matcher.group(1);
        String stackName = matcher.group(2);

        return assembleStackContent(localeName, stackName);
    }

    private StreamableResource assembleStackContent(String localeName, String stackName) throws IOException
    {
        localizationSetter.setNonPeristentLocaleFromLocaleName(localeName);

        JavaScriptStack stack = javascriptStackSource.getStack(stackName);
        List<Asset> libraries = stack.getJavaScriptLibraries();

        StreamableResource stackContent = assembleStackContent(localeName, stackName, libraries);

        return minificationEnabled ? resourceMinimizer.minimize(stackContent) : stackContent;
    }

    private StreamableResource assembleStackContent(String localeName, String stackName, List<Asset> libraries) throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(stream, "UTF-8");
        PrintWriter writer = new PrintWriter(osw, true);
        long lastModified = 0;

        StringBuilder description = new StringBuilder(String.format("'%s' JavaScript stack, for locale %s, resources=", stackName, localeName));
        String sep = "";

        JSONArray paths = new JSONArray();

        for (Asset library : libraries)
        {
            String path = library.toClientURL();

            paths.put(path);

            writer.format("\n/* %s */;\n", path);

            Resource resource = library.getResource();

            description.append(sep).append(resource.toString());
            sep = ", ";

            StreamableResource streamable = streamableResourceSource.getStreamableResource(resource,
                    StreamableResourceProcessing.FOR_AGGREGATION, resourceChangeTracker);

            streamable.streamTo(stream);

            lastModified = Math.max(lastModified, streamable.getLastModified());
        }

        writer.close();

        return new StreamableResourceImpl(
                description.toString(),
                JAVASCRIPT_CONTENT_TYPE, CompressionStatus.COMPRESSABLE, lastModified,
                new BytestreamCache(stream));
    }

    private StreamableResource compressStream(StreamableResource uncompressed) throws IOException
    {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        OutputStream compressor = new BufferedOutputStream(new GZIPOutputStream(compressed));

        uncompressed.streamTo(compressor);

        compressor.close();

        BytestreamCache cache = new BytestreamCache(compressed);

        return new StreamableResourceImpl(uncompressed.getDescription(), JAVASCRIPT_CONTENT_TYPE, CompressionStatus.COMPRESSED,
                uncompressed.getLastModified(), cache);
    }
}
