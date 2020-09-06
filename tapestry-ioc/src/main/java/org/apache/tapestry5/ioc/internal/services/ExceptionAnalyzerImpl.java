// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import java.util.*;

import org.apache.tapestry5.commons.services.*;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ExceptionAnalysis;
import org.apache.tapestry5.ioc.services.ExceptionAnalyzer;
import org.apache.tapestry5.ioc.services.ExceptionInfo;

public class ExceptionAnalyzerImpl implements ExceptionAnalyzer
{
    private final PropertyAccess propertyAccess;

    private final Set<String> throwableProperties;

    /**
     * A tuple used to communicate up a lavel both the exception info
     * and the next exception in the stack.
     */
    private static class ExceptionData
    {
        final ExceptionInfo exceptionInfo;
        final Throwable cause;

        public ExceptionData(ExceptionInfo exceptionInfo, Throwable cause)
        {
            this.exceptionInfo = exceptionInfo;
            this.cause = cause;
        }
    }

    public ExceptionAnalyzerImpl(PropertyAccess propertyAccess)
    {
        this.propertyAccess = propertyAccess;

        throwableProperties = CollectionFactory.newSet(this.propertyAccess.getAdapter(Throwable.class)
                .getPropertyNames());
    }

    @Override
    public ExceptionAnalysis analyze(Throwable rootException)
    {
        List<ExceptionInfo> list = CollectionFactory.newList();

        Throwable t = rootException;

        ExceptionInfo previousInfo = null;

        while (t != null)
        {
            ExceptionData data = extractData(t);

            ExceptionInfo info = data.exceptionInfo;

            if (addsValue(previousInfo, info))
            {
                list.add(info);
                previousInfo = info;
            }

            t = data.cause;
        }

        return new ExceptionAnalysisImpl(list);
    }

    /**
     * We want to filter out exceptions that do not provide any additional value. Additional value includes: an
     * exception message not present in the containing exception or a property value not present in the containing
     * exception. Also the first exception is always valued and the last exception (with the stack trace) is valued.
     *
     * @param previousInfo
     * @param info
     * @return
     */
    private boolean addsValue(ExceptionInfo previousInfo, ExceptionInfo info)
    {
        if (previousInfo == null)
            return true;

        if (!info.getStackTrace().isEmpty())
            return true;

        // TAP5-508: This adds back in a large number of frames that used to be squashed.
        if (!info.getClassName().equals(previousInfo.getClassName()))
            return true;

        if (!previousInfo.getMessage().contains(info.getMessage()))
            return true;

        for (String name : info.getPropertyNames())
        {
            if (info.getProperty(name).equals(previousInfo.getProperty(name)))
                continue;

            // Found something new and different at this level.

            return true;
        }

        // This exception adds nothing that is not present at a higher level.

        return false;
    }

    private ExceptionData extractData(Throwable t)
    {
        Map<String, Object> properties = CollectionFactory.newMap();

        ClassPropertyAdapter adapter = propertyAccess.getAdapter(t);

        Throwable cause = null;

        for (String name : adapter.getPropertyNames())
        {
            PropertyAdapter pa = adapter.getPropertyAdapter(name);

            if (!pa.isRead())
                continue;

            if (cause == null && Throwable.class.isAssignableFrom(pa.getType()))
            {
                // Ignore the property, but track it as the cause.

                Throwable nestedException = (Throwable) pa.get(t);

                // Handle the case where an exception is its own cause (avoid endless loop!)
                if (t != nestedException)
                    cause = nestedException;

                continue;
            }

            // Otherwise, ignore properties defined by the Throwable class

            if (throwableProperties.contains(name))
                continue;

            Object value = pa.get(t);

            if (value == null)
                continue;

            // An interesting property, let's save it for the analysis.

            properties.put(name, value);
        }

        // Provide the stack trace only at the deepest exception.

        List<StackTraceElement> stackTrace = Collections.emptyList();

        // Usually, I'd use a terniary expression here, but Generics gets in
        // the way here.

        if (cause == null)
            stackTrace = Arrays.asList(t.getStackTrace());

        ExceptionInfo info = new ExceptionInfoImpl(t, properties, stackTrace);

        return new ExceptionData(info, cause);
    }
}
