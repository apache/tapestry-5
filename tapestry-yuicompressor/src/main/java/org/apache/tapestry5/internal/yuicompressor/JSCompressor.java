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
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * JavaScript resource compressor based on the YUI {@link JavaScriptCompressor}.
 */
public class JSCompressor implements ResourceMinimizer
{
    private final Logger logger;

    private final OperationTracker tracker;

    public JSCompressor(Logger logger, OperationTracker tracker)
    {
        this.logger = logger;
        this.tracker = tracker;
    }

    private final ErrorReporter errorReporter = new ErrorReporter()
    {
        private String format(String message, int line, int lineOffset)
        {
            if (line < 0)
                return message;

            return String.format("(%d:%d): %s", line, lineOffset, message);
        }

        public void warning(String message, String sourceName, int line, String lineSource, int lineOffset)
        {
            logger.warn(format(message, line, lineOffset));
        }

        public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
                int lineOffset)
        {
            error(message, sourceName, line, lineSource, lineOffset);

            return new EvaluatorException(message);
        }

        public void error(String message, String sourceName, int line, String lineSource, int lineOffset)
        {
        }
    };

    public StreamableResource minimize(StreamableResource input) throws IOException
    {
        long startNanos = System.nanoTime();

        InputStream inputStream = input.openStream();

        final Reader reader = toReader(inputStream);

        ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);

        final Writer writer = new OutputStreamWriter(bos);

        TapestryInternalUtils.performIO(tracker, "Compressing JavaScript using YUICompressor", new IOOperation()
        {
            public void perform() throws IOException
            {
                try
                {
                    JavaScriptCompressor compressor = new JavaScriptCompressor(reader, errorReporter);

                    compressor.compress(writer, 0, true, false, false, false);
                }
                catch (EvaluatorException ex)
                {
                    throw new RuntimeException(String.format("Unable to compress JavaScript: %s",
                            InternalUtils.toMessage(ex)), ex);
                }
            }
        });

        inputStream.close();
        writer.close();

        long ellapsedNanos = System.nanoTime() - startNanos;

        // The content is minimized, but can still be (GZip) compressed.

        StreamableResource output = new StreamableResourceImpl(input.getContentType(),
                CompressionStatus.COMPRESSABLE, input.getLastModified(), new BytestreamCache(bos));

        if (logger.isDebugEnabled())
            logger.debug(String.format("Minimized %,d input bytes to %,d output bytes in %.2d ms", input.getSize(),
                    output.getSize(), ellapsedNanos / 1000.));

        return output;
    }

    private Reader toReader(InputStream input) throws IOException
    {
        return new InputStreamReader(input, "UTF-8");
    }
}
