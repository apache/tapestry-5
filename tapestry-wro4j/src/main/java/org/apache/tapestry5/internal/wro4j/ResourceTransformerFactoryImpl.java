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

import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.apache.tapestry5.wro4j.services.ResourceProcessor;
import org.apache.tapestry5.wro4j.services.ResourceProcessorSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

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

    public ResourceTransformer createCompiler(final String contentType, String processorName, final String sourceName, final String targetName)
    {
        final ResourceProcessor compiler = source.getProcessor(processorName);

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

                        InputStream result = compiler.process(description,
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
}
