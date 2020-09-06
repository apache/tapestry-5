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

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Reducer;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.RequestGlobals;
import org.apache.tapestry5.http.services.Session;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ExceptionAnalysis;
import org.apache.tapestry5.ioc.services.ExceptionAnalyzer;
import org.apache.tapestry5.ioc.services.ExceptionInfo;
import org.apache.tapestry5.services.ExceptionReportWriter;

import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExceptionReportWriterImpl implements ExceptionReportWriter
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

    private final static Mapper<ExceptionInfo, Flow<String>> EXCEPTION_INFO_TO_PROPERTY_NAMES =
            new Mapper<ExceptionInfo, Flow<String>>()
            {
                @Override
                public Flow<String> map(ExceptionInfo element)
                {
                    return F.flow(element.getPropertyNames());
                }
            };

    /**
     * A little closure that understands how to write a key/value pair representing a property.
     */
    interface PropertyWriter
    {
        void write(String name, Object value);
    }

    @Inject
    private ExceptionAnalyzer analyzer;

    @Inject
    private RequestGlobals requestGlobals;

    @Inject
    @Symbol(TapestryHttpSymbolConstants.CONTEXT_PATH)
    private String contextPath;

    @Override
    public void writeReport(PrintWriter writer, Throwable exception)
    {
        writeReport(writer, analyzer.analyze(exception));
    }

    private PropertyWriter newPropertyWriter(final PrintWriter writer, Iterable<String> names)
    {
        final int maxPropertyNameLength = F.flow(names).map(STRING_TO_LENGTH).reduce(MAX, 0);

        final String propertyNameFormat = "  %" + maxPropertyNameLength + "s: %s%n";

        return new PropertyWriter()
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
                            writer.printf(propertyNameFormat, name, i.next());
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

                writer.printf(propertyNameFormat, name, value);
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
    }

    @Override
    public void writeReport(final PrintWriter writer, ExceptionAnalysis analysis)
    {
        writer.printf("EXCEPTION STACK:%n%n");

        // Figure out what all the property names are so that we can set the width of the column that lists
        // property names.
        Flow<String> propertyNames = F.flow(analysis.getExceptionInfos())
                .mapcat(EXCEPTION_INFO_TO_PROPERTY_NAMES).append("Exception", "Message");

        PropertyWriter pw = newPropertyWriter(writer, propertyNames);

        boolean first = true;

        for (ExceptionInfo info : analysis.getExceptionInfos())
        {
            if (first)
            {
                writer.println();
                first = false;
            }

            pw.write("Exception", info.getClassName());
            pw.write("Message", info.getMessage());

            for (String name : info.getPropertyNames())
            {
                pw.write(name, info.getProperty(name));
            }
            if (!info.getStackTrace().isEmpty())
            {
                writer.printf("%n  Stack trace:%n%n");
                for (StackTraceElement e : info.getStackTrace())
                {
                    writer.printf("  - %s%n", e.toString());
                }
            }
            writer.println();
        }

        Request request = requestGlobals.getRequest();

        if (request != null)
        {
            // New PropertyWriter based on the lengths of parameter names and header names, and a sample of
            // the literal keys.

            pw = newPropertyWriter(writer,
                    F.flow(request.getParameterNames())
                            .concat(request.getHeaderNames())
                            .append("serverName", "removeHost"));

            writer.printf("REQUEST:%n%nBasic Information:%n%n");

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
            pw.write("remoteHost", request.getRemoteHost());

            writer.printf("%nHeaders:%n%n");

            for (String name : request.getHeaderNames())
            {
                pw.write(name, request.getHeader(name));
            }
            if (!request.getParameterNames().isEmpty())
            {
                writer.printf("%nParameters:%n");
                for (String name : request.getParameterNames())
                {
                    // TODO: Support multi-value parameters
                    pw.write(name, request.getParameters(name));
                }
            }

            Session session = request.getSession(false);

            if (session != null)
            {
                pw = newPropertyWriter(writer, session.getAttributeNames());

                writer.printf("%nSESSION:%n%n");

                for (String name : session.getAttributeNames())
                {
                    pw.write(name, session.getAttribute(name));
                }
            }
        }

        writer.printf("%nSYSTEM INFORMATION:");

        Runtime runtime = Runtime.getRuntime();

        writer.printf("%n%nMemory:%n  %,15d bytes free%n  %,15d bytes total%n  %,15d bytes max%n",
                runtime.freeMemory(),
                runtime.totalMemory(),
                runtime.maxMemory());

        Thread[] threads = TapestryInternalUtils.getAllThreads();

        int maxThreadNameLength = 0;

        for (Thread t : threads)
        {
            maxThreadNameLength = Math.max(maxThreadNameLength, t.getName().length());
        }

        String format = "%n%s %" + maxThreadNameLength + "s %s";

        writer.printf("%n%,d Threads:", threads.length);

        for (Thread t : threads)
        {
            writer.printf(format,
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
                writer.printf(", priority %d", t.getPriority());
            }
        }

        // Finish the final line.
        writer.println();
    }
}
