// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.yuicompressor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.tapestry5.internal.IOOperation;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.services.assets.BytestreamCache;
import org.apache.tapestry5.internal.services.assets.StreamableResourceImpl;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.assets.CompressionStatus;
import org.apache.tapestry5.services.assets.ResourceMinimizer;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;

/**
 * Base class for resource minimizers.
 * 
 * @since 5.3.0
 */
public abstract class AbstractMinimizer implements ResourceMinimizer
{
    private static final double NANOS_TO_MILLIS = 1.0d / 1000000.0d;

    private final Logger logger;

    private final OperationTracker tracker;

    private final String resourceType;

    public AbstractMinimizer(Logger logger, OperationTracker tracker, String resourceType)
    {
        this.logger = logger;
        this.tracker = tracker;
        this.resourceType = resourceType;
    }

    public StreamableResource minimize(StreamableResource input) throws IOException
    {
        long startNanos = System.nanoTime();

        InputStream inputStream = input.openStream();

        final Reader reader = toReader(inputStream);

        ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);

        final Writer writer = new OutputStreamWriter(bos);

        TapestryInternalUtils.performIO(tracker, "Minimizing " + resourceType, new IOOperation()
        {
            public void perform() throws IOException
            {
                try
                {
                    doMinimize(reader, writer);
                }
                catch (EvaluatorException ex)
                {
                    throw new RuntimeException(String.format("Unable to minimize %s: %s", resourceType,
                            InternalUtils.toMessage(ex)), ex);
                }
            }
        });

        inputStream.close();
        writer.close();

        // The content is minimized, but can still be (GZip) compressed.

        StreamableResource output = new StreamableResourceImpl(input.getContentType(), CompressionStatus.COMPRESSABLE,
                input.getLastModified(), new BytestreamCache(bos));

        long elapsedNanos = System.nanoTime() - startNanos;

        if (logger.isDebugEnabled())
        {
            double elapsedMillis = ((double) elapsedNanos) * NANOS_TO_MILLIS;

            logger.debug(String.format("Minimized %,d input bytes of %s to %,d output bytes in %.2f ms",
                    input.getSize(), resourceType, output.getSize(), elapsedMillis));
        }

        return output;
    }

    private Reader toReader(InputStream input) throws IOException
    {
        return new InputStreamReader(input, "UTF-8");
    }

    /**
     * Implemented in subclasses to do the actual work.
     * 
     * @param input
     *            content to minimize
     * @param output
     *            writer for minimized version of input
     */
    protected abstract void doMinimize(Reader input, Writer output) throws IOException;
}
