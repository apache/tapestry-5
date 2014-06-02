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

package org.apache.tapestry5.internal.services.exceptions;

import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Reducer;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ExceptionAnalysis;
import org.apache.tapestry5.ioc.services.ExceptionAnalyzer;
import org.apache.tapestry5.ioc.services.ExceptionInfo;
import org.apache.tapestry5.ioc.util.ExceptionUtils;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.exceptions.ExceptionReporter;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ExceptionReporterImpl implements ExceptionReporter
{
    private static final Reducer<Integer, Integer> MAX = new Reducer<Integer, Integer>()
    {
        @Override
        public Integer reduce(Integer accumulator, Integer element)
        {
            return Math.max(accumulator, element);
        }
    };

    private static final Mapper<String, Integer> STRING_TO_LENGTH = new Mapper<String, Integer>()
    {
        @Override
        public Integer map(String element)
        {
            return element.length();
        }
    };

    @Inject
    @Symbol(SymbolConstants.EXCEPTION_REPORTS_DIR)
    private File logDir;

    @Inject
    @Symbol(SymbolConstants.CONTEXT_PATH)
    private String contextPath;

    @Inject
    private ExceptionAnalyzer analyzer;

    private final AtomicInteger uid = new AtomicInteger();

    @Inject
    private Logger logger;

    @Inject
    private RequestGlobals requestGlobals;

    @Override
    public void reportException(Throwable exception)
    {
        Date date = new Date();
        String folderName = String.format("%tY-%<tm-%<td/%<tH/%<tM", date);
        String fileName = String.format(
                "exception-%tY%<tm%<td-%<tH%<tM%<tS-%<tL.%d.txt", date,
                uid.getAndIncrement());

        try
        {
            File folder = new File(logDir, folderName);
            folder.mkdirs();

            File log = new File(folder, fileName);

            writeExceptionToFile(exception, log);

            logger.warn(String.format("Wrote exception report to %s", toURI(log)));
        } catch (Exception ex)
        {
            logger.error(String.format("Unable to write exception report %s: %s",
                    fileName, ExceptionUtils.toMessage(ex)));

            logger.error("Original exception:", exception);
        }
    }

    private String toURI(File file)
    {
        try
        {
            return file.toURI().toString();
        } catch (Exception e)
        {
            return file.toString();
        }
    }

    private void writeExceptionToFile(Throwable exception, File log) throws IOException
    {
        log.createNewFile();
        ExceptionAnalysis analysis = analyzer.analyze(exception);
        PrintWriter writer = null;
        try
        {
            writer = new PrintWriter(log);
            writeException(writer, analysis);
        } finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

    interface PropertyWriter
    {
        void write(String name, Object value);
    }

    private final static Mapper<ExceptionInfo, Flow<String>> EXCEPTION_INFO_TO_PROPERTY_NAMES =
            new Mapper<ExceptionInfo, Flow<String>>()
            {
                @Override
                public Flow<String> map(ExceptionInfo element)
                {
                    return F.flow(element.getPropertyNames());
                }
            };

    private void writeException(final PrintWriter writer, ExceptionAnalysis analysis)
    {
        final Formatter f = new Formatter(writer);
        writer.print("EXCEPTION STACK:\n\n");
        Request request = requestGlobals.getRequest();

        // Figure out what all the property names are so that we can set the width of the column that lists
        // property names.
        Flow<String> propertyNames = F.flow(analysis.getExceptionInfos())
                .mapcat(EXCEPTION_INFO_TO_PROPERTY_NAMES).append("Exception type", "Message");

        if (request != null)
        {
            propertyNames = propertyNames.concat(request.getParameterNames()).concat(request.getHeaderNames());
        }

        final int maxPropertyNameLength = propertyNames.map(STRING_TO_LENGTH).reduce(MAX, 0);

        final String propertyNameFormat = "  %" + maxPropertyNameLength + "s: %s\n";

        PropertyWriter pw = new PropertyWriter()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public void write(String name, Object value)
            {
                if (value.getClass().isArray())
                {
                    write(name, toList(value));
                    return;
                }

                if (value instanceof Iterable)
                {
                    boolean first = true;
                    Iterable iterable = (Iterable) value;
                    Iterator i = iterable.iterator();
                    while (i.hasNext())
                    {
                        if (first)
                        {
                            f.format(propertyNameFormat, name, i.next());
                            first = false;
                        } else
                        {
                            for (int j = 0; j < maxPropertyNameLength + 4; j++)
                                writer.write(' ');

                            writer.println(i.next());
                        }
                    }
                    return;
                }

                // TODO: Handling of arrays & collections
                f.format(propertyNameFormat, name, value);
            }

            @SuppressWarnings({"rawtypes", "unchecked"})
            private List toList(Object array)
            {
                int count = Array.getLength(array);
                List result = new ArrayList(count);
                for (int i = 0; i < count; i++)
                {
                    result.add(Array.get(array, i));
                }
                return result;
            }
        };

        boolean first = true;

        for (ExceptionInfo info : analysis.getExceptionInfos())
        {
            if (first)
            {
                writer.println();
                first = false;
            }
            pw.write("Exception type", info.getClassName());
            pw.write("Message", info.getMessage());
            for (String name : info.getPropertyNames())
            {
                pw.write(name, info.getProperty(name));
            }
            if (!info.getStackTrace().isEmpty())
            {
                writer.write("\n  Stack trace:\n");
                for (StackTraceElement e : info.getStackTrace())
                {
                    f.format("  - %s\n", e.toString());
                }
            }
            writer.println();
        }

        if (request != null)
        {
            writer.print("REQUEST:\n\nBasic Information:\n");
            List<String> flags = CollectionFactory.newList();
            if (request.isXHR())
            {
                flags.add("XHR");
            }
            if (request.isRequestedSessionIdValid())
            {
                flags.add("requestedSessionIdValid");
            }
            if (request.isSecure())
            {
                flags.add("secure");
            }
            pw.write("contextPath", contextPath);
            if (!flags.isEmpty())
            {
                pw.write("flags", InternalUtils.joinSorted(flags));
            }
            pw.write("method", request.getMethod());
            pw.write("path", request.getPath());
            pw.write("locale", request.getLocale());
            pw.write("serverName", request.getServerName());
            writer.print("\nHeaders:\n");
            for (String name : request.getHeaderNames())
            {
                pw.write(name, request.getHeader(name));
            }
            if (!request.getParameterNames().isEmpty())
            {
                writer.print("\nParameters:\n");
                for (String name : request.getParameterNames())
                {
                    // TODO: Support multi-value parameters
                    pw.write(name, request.getParameters(name));
                }
            }
            // TODO: Session if it exists
        }

        writer.print("\nSYSTEM INFORMATION:");

        Runtime runtime = Runtime.getRuntime();

        f.format("\n\nMemory:\n  %,15d bytes free\n  %,15d bytes total\n  %,15d bytes max\n",
                runtime.freeMemory(),
                runtime.totalMemory(),
                runtime.maxMemory());

        Thread[] threads = TapestryInternalUtils.getAllThreads();

        int maxThreadNameLength = 0;

        for (Thread t : threads)
        {
            maxThreadNameLength = Math.max(maxThreadNameLength, t.getName().length());
        }

        String format = "\n%s %" + maxThreadNameLength + "s %s";

        f.format("\n%,d Threads:", threads.length);

        for (Thread t : threads)
        {
            f.format(format,
                    Thread.currentThread() == t ? "*" : " ",
                    t.getName(),
                    t.getState().name());
            if (t.isDaemon())
            {
                writer.write(", daemon");
            }
            if (!t.isAlive())
            {
                writer.write(", NOT alive");
            }
            if (t.isInterrupted())
            {
                writer.write(", interrupted");
            }
            if (t.getPriority() != Thread.NORM_PRIORITY)
            {
                f.format(", priority %d", t.getPriority());
            }
        }
        writer.println();

        f.close();
    }

}
