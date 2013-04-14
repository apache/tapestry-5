// Copyright 2013 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.assets.*;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.JavaScriptStackSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JavaScriptStackAssemblerImpl implements JavaScriptStackAssembler
{
    private static final String JAVASCRIPT_CONTENT_TYPE = "text/javascript";

    private ThreadLocale threadLocale;

    private final ResourceChangeTracker resourceChangeTracker;

    private final StreamableResourceSource streamableResourceSource;

    private final JavaScriptStackSource stackSource;

    private final AssetChecksumGenerator checksumGenerator;

    private final Map<String, StreamableResource> cache = CollectionFactory.newCaseInsensitiveMap();

    // TODO: Support for minimization
    // TODO: Support for aggregated CSS as well as aggregated JavaScript

    public JavaScriptStackAssemblerImpl(ThreadLocale threadLocale, ResourceChangeTracker resourceChangeTracker, StreamableResourceSource streamableResourceSource, JavaScriptStackSource stackSource, AssetChecksumGenerator checksumGenerator)
    {
        this.threadLocale = threadLocale;
        this.resourceChangeTracker = resourceChangeTracker;
        this.streamableResourceSource = streamableResourceSource;
        this.stackSource = stackSource;
        this.checksumGenerator = checksumGenerator;

        resourceChangeTracker.clearOnInvalidation(cache);
    }

    @Override
    public StreamableResource assembleJavaScriptResourceForStack(String stackName, boolean compress) throws IOException
    {
        Locale locale = threadLocale.getLocale();

        return assembleJavascriptResourceForStack(locale, stackName, compress);
    }

    private StreamableResource assembleJavascriptResourceForStack(Locale locale, String stackName, boolean compress) throws IOException
    {
        String key =
                String.format("%s[%s] %s",
                        stackName,
                        compress ? "COMPRESS" : "UNCOMPRESSED",
                        locale.toString());

        StreamableResource result = cache.get(key);

        if (result == null)
        {
            result = assemble(locale, stackName, compress);
            cache.put(key, result);
        }

        return result;
    }

    private StreamableResource assemble(Locale locale, String stackName, boolean compress) throws IOException
    {
        if (compress)
        {
            StreamableResource uncompressed = assembleJavascriptResourceForStack(locale, stackName, false);

            return new CompressedStreamableResource(uncompressed, checksumGenerator);
        }

        JavaScriptStack stack = stackSource.getStack(stackName);

        return assemble(locale.toString(), stackName, stack.getJavaScriptLibraries());
    }


    private StreamableResource assemble(String localeName, String stackName, List<Asset> libraries) throws IOException
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
            String path = library.getResource().toString();

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
                new BytestreamCache(stream), checksumGenerator);
    }


}
