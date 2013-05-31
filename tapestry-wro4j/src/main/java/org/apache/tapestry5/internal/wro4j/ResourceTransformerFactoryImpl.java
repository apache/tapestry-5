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

import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.assets.BytestreamCache;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.apache.tapestry5.wro4j.services.ResourceProcessor;
import org.apache.tapestry5.wro4j.services.ResourceProcessorSource;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.Adler32;

public class ResourceTransformerFactoryImpl implements ResourceTransformerFactory
{
    private static final double NANOS_TO_MILLIS = 1.0d / 1000000.0d;

    private final Logger logger;

    private final ResourceProcessorSource source;

    private final OperationTracker tracker;

    public ResourceTransformerFactoryImpl(Logger logger, ResourceProcessorSource source, OperationTracker tracker)
    {
        this.logger = logger;
        this.source = source;
        this.tracker = tracker;
    }


    static class Compiled
    {
        /**
         * Checksum of the raw source file.
         */
        final long checksum;

        private final BytestreamCache bytestreamCache;


        Compiled(long checksum, InputStream stream) throws IOException
        {
            this.checksum = checksum;

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

    private long toChecksum(Resource resource) throws IOException
    {
        Adler32 checksum = new Adler32();

        byte[] buffer = new byte[1024];

        InputStream is = null;

        try
        {
            is = resource.openStream();

            while (true)
            {
                int length = is.read(buffer);

                if (length < 0)
                {
                    break;
                }

                checksum.update(buffer, 0, length);
            }

            // Reduces it down to just 32 bits which we express in hex.'
            return checksum.getValue();
        } finally
        {
            is.close();
        }
    }

    public ResourceTransformer createCompiler(final String contentType, String processorName, final String sourceName, final String targetName, boolean enableCache)
    {
        // This does the real work:
        ResourceProcessor resourceProcessor = source.getProcessor(processorName);

        // And this adapts it to the API.
        final ResourceTransformer coreCompiler = createCoreCompiler(contentType, sourceName, targetName, resourceProcessor);

        return enableCache ? wrapWithCaching(coreCompiler, targetName) : coreCompiler;
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

                return tracker.perform(description, new IOOperation<InputStream>()
                {
                    public InputStream perform() throws IOException
                    {
                        final long startTime = System.nanoTime();

                        InputStream result = resourceProcessor.process(description,
                                source.toURL().toString(),
                                source.openStream(), contentType);

                        final long elapsedTime = System.nanoTime() - startTime;

                        logger.info(String.format("Compiled %s to %s in %.2f ms",
                                source, targetName,
                                ((double) elapsedTime) * NANOS_TO_MILLIS));

                        return result;
                    }
                });
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
    private ResourceTransformer wrapWithCaching(final ResourceTransformer core, final String targetName)
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
                long checksum = toChecksum(source);

                Compiled compiled = cache.get(source);

                if (compiled != null && compiled.checksum == checksum)
                {
                    logger.info(String.format("Resource %s is unchanged; serving compiled %s content from cache",
                            source, targetName));

                    return compiled.openStream();
                }

                InputStream is = core.transform(source, dependencies);

                // There's probably a race condition here if the source changes as we are compiling it.
                compiled = new Compiled(checksum, is);

                cache.put(source, compiled);

                return compiled.openStream();
            }
        };
    }
}
