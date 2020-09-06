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
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.assets.*;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.JavaScriptStackSource;
import org.apache.tapestry5.services.javascript.JavaScriptAggregationStrategy;
import org.apache.tapestry5.services.javascript.ModuleManager;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class JavaScriptStackAssemblerImpl implements JavaScriptStackAssembler
{
    private static final ContentType JAVASCRIPT_CONTENT_TYPE = new ContentType("text/javascript;charset=utf-8");

    private final ThreadLocale threadLocale;

    private final ResourceChangeTracker resourceChangeTracker;

    private final StreamableResourceSource streamableResourceSource;

    private final JavaScriptStackSource stackSource;

    private final AssetChecksumGenerator checksumGenerator;

    private final ModuleManager moduleManager;

    private final ResourceMinimizer resourceMinimizer;

    private final boolean minificationEnabled;

    private final Map<String, StreamableResource> cache = Collections.synchronizedMap(CollectionFactory.<StreamableResource>newCaseInsensitiveMap());

    private class Parameters
    {
        final Locale locale;

        final String stackName;

        final boolean compress;

        final JavaScriptAggregationStrategy javascriptAggregationStrategy;

        private Parameters(Locale locale, String stackName, boolean compress, JavaScriptAggregationStrategy javascriptAggregationStrategy)
        {
            this.locale = locale;
            this.stackName = stackName;
            this.compress = compress;
            this.javascriptAggregationStrategy = javascriptAggregationStrategy;
        }

        Parameters disableCompress()
        {
            return new Parameters(locale, stackName, false, javascriptAggregationStrategy);
        }
    }

    // TODO: Support for aggregated CSS as well as aggregated JavaScript

    public JavaScriptStackAssemblerImpl(ThreadLocale threadLocale, ResourceChangeTracker resourceChangeTracker, StreamableResourceSource streamableResourceSource,
                                        JavaScriptStackSource stackSource, AssetChecksumGenerator checksumGenerator, ModuleManager moduleManager,
                                        ResourceMinimizer resourceMinimizer,
                                        @Symbol(SymbolConstants.MINIFICATION_ENABLED)
                                        boolean minificationEnabled)
    {
        this.threadLocale = threadLocale;
        this.resourceChangeTracker = resourceChangeTracker;
        this.streamableResourceSource = streamableResourceSource;
        this.stackSource = stackSource;
        this.checksumGenerator = checksumGenerator;
        this.moduleManager = moduleManager;
        this.resourceMinimizer = resourceMinimizer;
        this.minificationEnabled = minificationEnabled;

        resourceChangeTracker.clearOnInvalidation(cache);
    }

    public StreamableResource assembleJavaScriptResourceForStack(String stackName, boolean compress, JavaScriptAggregationStrategy javascriptAggregationStrategy) throws IOException
    {
        Locale locale = threadLocale.getLocale();

        return assembleJavascriptResourceForStack(new Parameters(locale, stackName, compress, javascriptAggregationStrategy));
    }

    private StreamableResource assembleJavascriptResourceForStack(Parameters parameters) throws IOException
    {
        String key =
                String.format("%s[%s] %s",
                        parameters.stackName,
                        parameters.compress ? "COMPRESS" : "UNCOMPRESSED",
                        parameters.locale.toString());

        StreamableResource result = cache.get(key);

        if (result == null)
        {
            result = assemble(parameters);
            cache.put(key, result);
        }

        return result;
    }

    private StreamableResource assemble(Parameters parameters) throws IOException
    {
        if (parameters.compress)
        {
            StreamableResource uncompressed = assembleJavascriptResourceForStack(parameters.disableCompress());

            return new CompressedStreamableResource(uncompressed, checksumGenerator);
        }

        JavaScriptStack stack = stackSource.getStack(parameters.stackName);

        return assembleStreamableForStack(parameters.locale.toString(), parameters, stack.getJavaScriptLibraries(), stack.getModules());
    }

    interface StreamableReader
    {
        /**
         * Reads the content of a StreamableResource as a UTF-8 string, and optionally transforms it in some way.
         */
        String read(StreamableResource resource) throws IOException;
    }

    static String getContent(StreamableResource resource) throws IOException
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(resource.getSize());
        resource.streamTo(bos);

        return new String(bos.toByteArray(), "UTF-8");
    }


    final StreamableReader libraryReader = new StreamableReader()
    {
        public String read(StreamableResource resource) throws IOException
        {
            return getContent(resource);
        }
    };

    private final static Pattern DEFINE = Pattern.compile("\\bdefine\\s*\\((?!\\s*['\"])");

    private static class ModuleReader implements StreamableReader
    {
        final String moduleName;

        private ModuleReader(String moduleName)
        {
            this.moduleName = moduleName;
        }

        public String read(StreamableResource resource) throws IOException
        {
            String content = getContent(resource);

            return transform(content);
        }

        public String transform(String moduleContent)
        {
            return DEFINE.matcher(moduleContent).replaceFirst("define(\"" + moduleName + "\",");
        }
    }


    private class Assembly
    {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2000);
        final PrintWriter writer;
        long lastModified = 0;
        final StringBuilder description;
        private String sep = "";

        private Assembly(String description) throws UnsupportedEncodingException
        {
            writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));

            this.description = new StringBuilder(description);
        }

        void add(Resource resource, StreamableReader reader) throws IOException
        {
            writer.format("\n/* %s */;\n", resource.toString());

            description.append(sep).append(resource.toString());
            sep = ", ";

            StreamableResource streamable = streamableResourceSource.getStreamableResource(resource,
                    StreamableResourceProcessing.FOR_AGGREGATION, resourceChangeTracker);

            writer.print(reader.read(streamable));

            lastModified = Math.max(lastModified, streamable.getLastModified());
        }

        StreamableResource finish()
        {
            writer.close();

            return new StreamableResourceImpl(
                    description.toString(),
                    JAVASCRIPT_CONTENT_TYPE, CompressionStatus.COMPRESSABLE, lastModified,
                    new BytestreamCache(outputStream), checksumGenerator, null);
        }
    }

    private StreamableResource assembleStreamableForStack(String localeName, Parameters parameters,
                                                          List<Asset> libraries, List<String> moduleNames) throws IOException
    {
        Assembly assembly = new Assembly(String.format("'%s' JavaScript stack, for locale %s, resources=", parameters.stackName, localeName));

        for (Asset library : libraries)
        {
            Resource resource = library.getResource();

            assembly.add(resource, libraryReader);
        }

        for (String moduleName : moduleNames)
        {
            Resource resource = moduleManager.findResourceForModule(moduleName);

            if (resource == null)
            {
                throw new IllegalArgumentException(String.format("Could not identify a resource for module name '%s'.", moduleName));
            }

            assembly.add(resource, new ModuleReader(moduleName));
        }

        StreamableResource streamable = assembly.finish();

        if (minificationEnabled && parameters.javascriptAggregationStrategy.enablesMinimize())
        {
            return resourceMinimizer.minimize(streamable);
        }

        return streamable;
    }
}
