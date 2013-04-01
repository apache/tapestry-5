// Copyright 2011, 2012 The Apache Software Foundation
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

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.assets.AssetChecksumGenerator;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JavaScript resource minimizer based on the YUI {@link JavaScriptCompressor}.
 *
 * @since 5.3
 */
public class JavaScriptResourceMinimizer extends AbstractMinimizer
{
    private final static int RANGE = 5;

    private enum Where
    {
        EXACT, NEAR, FAR
    }

    private static final String[] IGNORED_WARNINGS = {
            "Try to use a single 'var' statement per scope.",
            "Using 'eval' is not recommended",
            "has already been declared in the same scope"
    };

    public JavaScriptResourceMinimizer(Logger logger, OperationTracker tracker, AssetChecksumGenerator checksumGenerator)
    {
        super(logger, tracker, checksumGenerator, "JavaScript");
    }

    protected void doMinimize(final StreamableResource resource, Writer output) throws IOException
    {
        final Set<Integer> errorLines = CollectionFactory.newSet();

        final Runnable identifySource = new Runnable()
        {
            boolean sourceIdentified = false;

            public void run()
            {
                if (!sourceIdentified)
                {
                    logger.error(String.format("JavaScript compression problems for resource %s:",
                            resource.getDescription()));
                    sourceIdentified = true;
                }
            }
        };

        final AtomicInteger warningCount = new AtomicInteger();

        Runnable identifyWarnings = new Runnable()
        {
            public void run()
            {
                if (warningCount.get() > 0)
                {
                    logger.error(String.format("%,d compression warnings; enable warning logging of %s to see details.",
                            warningCount.get(),
                            logger.getName()));
                }
            }
        };

        ErrorReporter errorReporter = new ErrorReporter()
        {
            private String format(String message, int line, int lineOffset)
            {
                if (line < 0)
                    return message;

                return String.format("(%d:%d): %s", line, lineOffset, message);
            }

            public void warning(String message, String sourceName, int line, String lineSource, int lineOffset)
            {
                for (String ignored : IGNORED_WARNINGS)
                {
                    if (message.contains(ignored))
                    {
                        return;
                    }
                }

                identifySource.run();

                errorLines.add(line);

                if (logger.isWarnEnabled())
                {
                    logger.warn(format(message, line, lineOffset));
                } else
                {
                    warningCount.incrementAndGet();
                }
            }

            public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
                                                   int lineOffset)
            {
                error(message, sourceName, line, lineSource, lineOffset);

                return new EvaluatorException(message);
            }

            public void error(String message, String sourceName, int line, String lineSource, int lineOffset)
            {
                identifySource.run();

                errorLines.add(line);

                logger.error(format(message, line, lineOffset));
            }

        };

        Reader reader = toReader(resource);

        try
        {
            JavaScriptCompressor compressor = new JavaScriptCompressor(reader, errorReporter);
            compressor.compress(output, -1, true, true, false, false);

            identifyWarnings.run();

        } catch (EvaluatorException ex)
        {
            identifySource.run();

            logInputLines(resource, errorLines);

            recoverFromException(ex, resource, output);

        } catch (Exception ex)
        {
            identifySource.run();

            recoverFromException(ex, resource, output);
        }

        reader.close();
    }

    private void recoverFromException(Exception ex, StreamableResource resource, Writer output) throws IOException
    {
        logger.error(InternalUtils.toMessage(ex), ex);

        streamUnminimized(resource, output);
    }

    private void streamUnminimized(StreamableResource resource, Writer output) throws IOException
    {
        Reader reader = toReader(resource);

        char[] buffer = new char[5000];

        try
        {

            while (true)
            {
                int length = reader.read(buffer);

                if (length < 0)
                {
                    break;
                }

                output.write(buffer, 0, length);
            }
        } finally
        {
            reader.close();
        }
    }

    private void logInputLines(StreamableResource resource, Set<Integer> lines)
    {
        int last = -1;

        try
        {
            LineNumberReader lnr = new LineNumberReader(toReader(resource));

            while (true)
            {
                String line = lnr.readLine();

                if (line == null) break;

                int lineNumber = lnr.getLineNumber();

                Where where = where(lineNumber, lines);

                if (where == Where.FAR)
                {
                    continue;
                }

                // Add a blank line to separate non-consecutive parts of the content.
                if (last > 0 && last + 1 != lineNumber)
                {
                    logger.error("");
                }

                String formatted = String.format("%s%6d %s",
                        where == Where.EXACT ? "*" : " ",
                        lineNumber,
                        line);

                logger.error(formatted);

                last = lineNumber;
            }

            lnr.close();

        } catch (IOException ex)
        { // Ignore.
        }

    }

    private Where where(int lineNumber, Set<Integer> lines)
    {
        if (lines.contains(lineNumber))
        {
            return Where.EXACT;
        }

        for (int line : lines)
        {
            if (Math.abs(lineNumber - line) < RANGE)
            {
                return Where.NEAR;
            }
        }

        return Where.FAR;
    }
}