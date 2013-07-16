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

package org.apache.tapestry5.internal.wro4j;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.assets.BytestreamCache;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.apache.tapestry5.wro4j.WRO4JSymbols;
import org.apache.tapestry5.wro4j.services.ResourceProcessor;
import org.apache.tapestry5.wro4j.services.ResourceProcessorSource;
import org.slf4j.Logger;

import java.io.*;
import java.util.Map;

public class ResourceTransformerFactoryImpl implements ResourceTransformerFactory
{
    private final Logger logger;

    private final ResourceProcessorSource source;

    private final OperationTracker tracker;

    private final boolean productionMode;

    private final File cacheDir;

    public ResourceTransformerFactoryImpl(Logger logger, ResourceProcessorSource source, OperationTracker tracker,
                                          @Symbol(SymbolConstants.PRODUCTION_MODE)
                                          boolean productionMode,
                                          @Symbol(WRO4JSymbols.CACHE_DIR)
                                          String cacheDir)
    {
        this.logger = logger;
        this.source = source;
        this.tracker = tracker;
        this.productionMode = productionMode;

        this.cacheDir = new File(cacheDir);

        if (!productionMode)
        {
            logger.info(String.format("Using %s to store compiled assets (development mode only).", cacheDir));
        }
    }

    @PostInjection
    public void createCacheDir()
    {
        cacheDir.mkdirs();
    }

    static class Compiled extends ContentChangeTracker
    {
        private BytestreamCache bytestreamCache;

        Compiled(Resource root)
        {
            addDependency(root);
        }

        void store(InputStream stream) throws IOException
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            TapestryInternalUtils.copy(stream, bos);

            stream.close();
            bos.close();

            this.bytestreamCache = new BytestreamCache(bos);
        }

        InputStream openStream()
        {
            return bytestreamCache.openStream();
        }
    }


    public ResourceTransformer createCompiler(final String contentType, String processorName, final String sourceName, final String targetName, CacheMode cacheMode)
    {
        // This does the real work:
        ResourceProcessor resourceProcessor = source.getProcessor(processorName);

        // And this adapts it to the API.
        ResourceTransformer coreCompiler = createCoreCompiler(contentType, sourceName, targetName, resourceProcessor);

        return createCompiler(contentType, sourceName, targetName, coreCompiler, cacheMode);

    }

    public ResourceTransformer createCompiler(String contentType, String sourceName, String targetName, ResourceTransformer transformer, CacheMode cacheMode)
    {
        ResourceTransformer trackingCompiler = wrapWithTracking(sourceName, targetName, transformer);

        if (productionMode)
        {
            return trackingCompiler;
        }

        ResourceTransformer timingCompiler = wrapWithTiming(targetName, trackingCompiler);

        switch (cacheMode)
        {
            case NONE:

                return timingCompiler;

            case SINGLE_FILE:

                return wrapWithFileSystemCaching(timingCompiler, targetName);

            case MULTIPLE_FILE:

                return wrapWithInMemoryCaching(timingCompiler, targetName);
        }

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private ResourceTransformer createCoreCompiler(final String contentType, final String sourceName, final String targetName, final ResourceProcessor resourceProcessor)
    {
        return new ResourceTransformer()
        {
            public String getTransformedContentType()
            {
                return contentType;
            }

            public InputStream transform(final Resource source, ResourceDependencies dependencies) throws IOException
            {
                final String description = String.format("Compiling %s from %s to %s", source, sourceName, targetName);

                InputStream result = resourceProcessor.process(description,
                        source.toURL().toString(),
                        source.openStream(), contentType);

                return result;

            }
        };
    }

    private ResourceTransformer wrapWithTracking(final String sourceName, final String targetName, final ResourceTransformer core)
    {
        return new ResourceTransformer()
        {
            public String getTransformedContentType()
            {
                return core.getTransformedContentType();
            }

            public InputStream transform(final Resource source, final ResourceDependencies dependencies) throws IOException
            {
                final String description = String.format("Compiling %s from %s to %s", source, sourceName, targetName);

                return tracker.perform(description, new IOOperation<InputStream>()
                {
                    public InputStream perform() throws IOException
                    {
                        return core.transform(source, dependencies);
                    }
                });
            }
        };
    }

    private ResourceTransformer wrapWithTiming(final String targetName, final ResourceTransformer coreCompiler)
    {
        return new ResourceTransformer()
        {
            public String getTransformedContentType()
            {
                return coreCompiler.getTransformedContentType();
            }

            public InputStream transform(final Resource source, final ResourceDependencies dependencies) throws IOException
            {
                final long startTime = System.nanoTime();

                InputStream result = coreCompiler.transform(source, dependencies);

                final long elapsedTime = System.nanoTime() - startTime;

                logger.info(String.format("Compiled %s to %s in %.2f ms",
                        source, targetName,
                        ResourceTransformUtils.nanosToMillis(elapsedTime)));

                return result;
            }
        };
    }

    /**
     * Caching is not needed in production, because caching of streamable resources occurs at a higher level
     * (possibly after sources have been aggregated and minimized and gzipped). However, in development, it is
     * very important to avoid costly CoffeeScript compilation (or similar operations); Tapestry's caching is
     * somewhat primitive: a change to *any* resource in a given domain results in the cache of all of those resources
     * being discarded.
     */
    private ResourceTransformer wrapWithInMemoryCaching(final ResourceTransformer core, final String targetName)
    {
        return new ResourceTransformer()
        {
            final Map<Resource, Compiled> cache = CollectionFactory.newConcurrentMap();

            public String getTransformedContentType()
            {
                return core.getTransformedContentType();
            }

            public InputStream transform(Resource source, ResourceDependencies dependencies) throws IOException
            {
                Compiled compiled = cache.get(source);

                if (compiled != null && !compiled.dirty())
                {
                    logger.info(String.format("Resource %s and dependencies are unchanged; serving compiled %s content from in-memory cache",
                            source, targetName));

                    return compiled.openStream();
                }

                compiled = new Compiled(source);

                InputStream is = core.transform(source, new ResourceDependenciesSplitter(dependencies, compiled));

                compiled.store(is);

                cache.put(source, compiled);

                return compiled.openStream();
            }
        };
    }

    private ResourceTransformer wrapWithFileSystemCaching(final ResourceTransformer core, final String targetName)
    {
        return new ResourceTransformer()
        {
            public String getTransformedContentType()
            {
                return core.getTransformedContentType();
            }

            public InputStream transform(Resource source, ResourceDependencies dependencies) throws IOException
            {
                long checksum = ResourceTransformUtils.toChecksum(source);

                String fileName = Long.toHexString(checksum) + "-" + source.getFile();

                File cacheFile = new File(cacheDir, fileName);

                if (cacheFile.exists())
                {
                    logger.debug(String.format("Serving up compiled %s content for %s from file system cache", targetName, source));

                    return new BufferedInputStream(new FileInputStream(cacheFile));
                }

                InputStream compiled = core.transform(source, dependencies);

                // We need the InputStream twice; once to return, and once to write out to the cache file for later.

                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                TapestryInternalUtils.copy(compiled, bos);

                BytestreamCache cache = new BytestreamCache(bos);

                writeToCacheFile(cacheFile, cache.openStream());

                return cache.openStream();
            }
        };
    }

    private void writeToCacheFile(File file, InputStream stream) throws IOException
    {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));

        TapestryInternalUtils.copy(stream, outputStream);

        outputStream.close();
    }
}
