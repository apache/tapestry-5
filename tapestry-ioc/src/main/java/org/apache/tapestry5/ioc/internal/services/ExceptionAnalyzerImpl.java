// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.*;

import java.util.*;

public class ExceptionAnalyzerImpl implements ExceptionAnalyzer
{
    private final PropertyAccess propertyAccess;

    private final Set<String> throwableProperties;

    public ExceptionAnalyzerImpl(PropertyAccess propertyAccess)
    {
        this.propertyAccess = propertyAccess;

        throwableProperties = CollectionFactory.newSet(this.propertyAccess.getAdapter(Throwable.class)
                .getPropertyNames());
    }

    public ExceptionAnalysis analyze(Throwable rootException)
    {
        List<ExceptionInfo> list = CollectionFactory.newList();

        Throwable t = rootException;

        ExceptionInfo previousInfo = null;

        while (t != null)
        {
            ExceptionInfo info = extractInfo(t);

            if (addsValue(previousInfo, info))
            {
                list.add(info);
                previousInfo = info;
            }

            t = t.getCause();
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
        if (previousInfo == null) return true;

        if (!info.getStackTrace().isEmpty()) return true;

        if (!previousInfo.getMessage().contains(info.getMessage())) return true;

        for (String name : info.getPropertyNames())
        {
            if (info.getProperty(name).equals(previousInfo.getProperty(name))) continue;

            // Found something new and different at this level.

            return true;
        }

        // This exception adds nothing that is not present at a higher level.

        return false;
    }

    private ExceptionInfo extractInfo(Throwable t)
    {
        Map<String, Object> properties = CollectionFactory.newMap();

        ClassPropertyAdapter adapter = propertyAccess.getAdapter(t);

        for (String name : adapter.getPropertyNames())
        {
            if (throwableProperties.contains(name)) continue;

            if (!adapter.getPropertyAdapter(name).isRead()) continue;

            Object value = adapter.get(t, name);

            if (value == null) continue;

            // An interesting property, let's save it for the analysis.

            properties.put(name, value);
        }

        List<StackTraceElement> stackTrace = Collections.emptyList();

        // Usually, I'd use a terniary expression here, but Generics gets in
        // the way here.

        if (t.getCause() == null)
            stackTrace = Arrays.asList(t.getStackTrace());

        return new ExceptionInfoImpl(t, properties, stackTrace);
    }
}
