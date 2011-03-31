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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.tapestry5.ioc.OperationTracker;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * JavaScript resource minimizer based on the YUI {@link JavaScriptCompressor}.
 */
public class JavaScriptResourceMinimizer extends AbstractMinimizer
{
    private final ErrorReporter errorReporter;

    public JavaScriptResourceMinimizer(final Logger logger, OperationTracker tracker)
    {
        super(logger, tracker, "JavaScript");

        errorReporter = new ErrorReporter()
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
                logger.error(format(message, line, lineOffset));
            }
        };

    }

    protected void doMinimize(Reader input, Writer output) throws IOException
    {
        JavaScriptCompressor compressor = new JavaScriptCompressor(input, errorReporter);

        compressor.compress(output, -1, true, false, false, false);
    }
}
