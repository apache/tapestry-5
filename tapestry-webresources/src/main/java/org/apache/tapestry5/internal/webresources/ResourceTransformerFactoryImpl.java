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

package org.apache.tapestry5.internal.webresources;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.assets.BytestreamCache;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.apache.tapestry5.webresources.WebResourcesSymbols;
import org.slf4j.Logger;

import java.io.*;
import java.util.Map;

public class ResourceTransformerFactoryImpl implements ResourceTransformerFactory
{
    private final Logger logger;

    private final OperationTracker tracker;

    private final boolean productionMode;

    private final File cacheDir;

    public ResourceTransformerFactoryImpl(Logger logger, OperationTracker tracker,
                                          @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
                                          boolean productionMode,
                                          @Symbol(WebResourcesSymbols.CACHE_DIR)
                                          String cacheDir)
    {
        this.logger = logger;
        this.tracker = tracker;
        this.productionMode = productionMode;

        this.cacheDir = new File(cacheDir);

        if (!productionMode)
        {
            logger.info(String.format("Using %s to store compiled assets (development mode only).", cacheDir));
        }
    }

    @PostInjection
    public void createCacheDir(@Symbol(SymbolConstants.RESTRICTIVE_ENVIRONMENT) boolean restrictive)
    {
        if (!restrictive)
        {
            cacheDir.mkdirs();
        }
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


    @Override
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

            default:

                throw new IllegalStateException();
        }
    }

    private ResourceTransformer wrapWithTracking(final String sourceName, final String targetName, ResourceTransformer core)
    {
        return new DelegatingResourceTransformer(core)
        {
            @Override
            public InputStream transform(final Resource source, final ResourceDependencies dependencies) throws IOException
            {
                final String description = String.format("Compiling %s from %s to %s", source, sourceName, targetName);

                return tracker.perform(description, new IOOperation<InputStream>()
                {
                    @Override
                    public InputStream perform() throws IOException
                    {
                        return delegate.transform(source, dependencies);
                    }
                });
            }
        };
    }

    private ResourceTransformer wrapWithTiming(final String targetName, ResourceTransformer coreCompiler)
    {
        return new DelegatingResourceTransformer(coreCompiler)
        {
            @Override
            public InputStream transform(final Resource source, final ResourceDependencies dependencies) throws IOException
            {
                final long startTime = System.nanoTime();

                InputStream result = delegate.transform(source, dependencies);

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
    private ResourceTransformer wrapWithInMemoryCaching( ResourceTransformer core, final String targetName)
    {
        return new DelegatingResourceTransformer(core)
        {
            final Map<Resource, Compiled> cache = CollectionFactory.newConcurrentMap();

            @Override
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

                InputStream is = delegate.transform(source, new ResourceDependenciesSplitter(dependencies, compiled));

                compiled.store(is);

                is.close();

                cache.put(source, compiled);

                return compiled.openStream();
            }
        };
    }

    private ResourceTransformer wrapWithFileSystemCaching( ResourceTransformer core, final String targetName)
    {
        return new DelegatingResourceTransformer(core)
        {
            @Override
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

                InputStream compiled = delegate.transform(source, dependencies);

                // We need the InputStream twice; once to return, and once to write out to the cache file for later.

                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                TapestryInternalUtils.copy(compiled, bos);

                compiled.close();

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
