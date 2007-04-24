// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry.ioc.services.ExceptionAnalysis;
import org.apache.tapestry.ioc.services.ExceptionAnalyzer;
import org.apache.tapestry.ioc.services.ExceptionInfo;
import org.apache.tapestry.ioc.services.PropertyAccess;

/**
 * 
 */
public class ExceptionAnalyzerImpl implements ExceptionAnalyzer
{
    private final PropertyAccess _propertyAccess;

    private final Set<String> _throwableProperties;

    public ExceptionAnalyzerImpl(PropertyAccess propertyAccess)
    {
        _propertyAccess = propertyAccess;

        _throwableProperties = newSet(_propertyAccess.getAdapter(Throwable.class)
                .getPropertyNames());
    }

    public ExceptionAnalysis analyze(Throwable rootException)
    {
        List<ExceptionInfo> list = CollectionFactory.newList();

        Throwable t = rootException;

        while (t != null)
        {
            ExceptionInfo info = extractInfo(t);

            list.add(info);

            t = t.getCause();
        }

        return new ExceptionAnalysisImpl(list);
    }

    private ExceptionInfo extractInfo(Throwable t)
    {
        Map<String, Object> properties = newMap();

        ClassPropertyAdapter adapter = _propertyAccess.getAdapter(t);

        for (String name : adapter.getPropertyNames())
        {
            if (_throwableProperties.contains(name)) continue;

            Object value = adapter.get(t, name);

            if (value == null) continue;

            // An interesting property, let's save it for the analysis.

            properties.put(name, value);
        }

        List<String> stackTrace = Collections.emptyList();

        // Usually, I'd use a terniary expression here, but Generics gets in
        // the way here.

        if (t.getCause() == null) stackTrace = extractStackTrace(t);

        return new ExceptionInfoImpl(t, properties, stackTrace);
    }

    private List<String> extractStackTrace(Throwable t)
    {
        List<String> trace = newList();

        for (StackTraceElement e : t.getStackTrace())
        {
            // Edit out IoC Proxy classes. They always start with a '$'
            // and don't have any line number information.

            if (e.getClassName().startsWith("$") && e.getLineNumber() < 0) continue;

            trace.add(e.toString());
        }

        return trace;
    }
}
