// Copyright 2006, 2008 The Apache Software Foundation
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

import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ExceptionInfo;

import static java.util.Collections.unmodifiableList;
import java.util.List;
import java.util.Map;

public class ExceptionInfoImpl implements ExceptionInfo
{
    private final String _className;

    private final String _message;

    private final Map<String, Object> _properties;

    private final List<StackTraceElement> _stackTrace;

    public ExceptionInfoImpl(Throwable t, Map<String, Object> properties, List<StackTraceElement> stackTrace)
    {
        _className = t.getClass().getName();
        _message = t.getMessage() != null ? t.getMessage() : "";

        _properties = properties;
        _stackTrace = unmodifiableList(stackTrace);
    }

    public String getClassName()
    {
        return _className;
    }

    public String getMessage()
    {
        return _message;
    }

    public Object getProperty(String name)
    {
        return _properties.get(name);
    }

    public List<String> getPropertyNames()
    {
        return InternalUtils.sortedKeys(_properties);
    }

    public List<StackTraceElement> getStackTrace()
    {
        return _stackTrace;
    }

}
