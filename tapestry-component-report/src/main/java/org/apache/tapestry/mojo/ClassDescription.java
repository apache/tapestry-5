// Copyright 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry.mojo;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.Map;

public class ClassDescription
{
    private final String superClassName;

    private final String className;

    private final String description;

    private final boolean supportsInformalParameters;

    private final Map<String, ParameterDescription> parameters = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, String> publishedParameters = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, String> events = CollectionFactory.newCaseInsensitiveMap();

    private final String since;

    public ClassDescription(String className, String superClassName, String description,
                            boolean supportsInformalParameters, String since)
    {
        this.className = className;
        this.superClassName = superClassName;
        this.description = description;
        this.supportsInformalParameters = supportsInformalParameters;
        this.since = since;
    }

    public String getClassName()
    {
        return className;
    }

    public String getDescription()
    {
        return description;
    }

    public Map<String, ParameterDescription> getParameters()
    {
        return parameters;
    }

    public String getSuperClassName()
    {
        return superClassName;
    }

    public boolean isSupportsInformalParameters()
    {
        return supportsInformalParameters;
    }

    public String getSince()
    {
        return since;
    }

    /**
     * Case insensitive map, keyed on parameter name, value is class name of component from which the parameter is
     * published.
     */
    public Map<String, String> getPublishedParameters()
    {
        return publishedParameters;
    }

    /**
     * Case insentive map, keyes on event name, value is optional description (often blank).
     */
    public Map<String, String> getEvents()
    {
        return events;
    }
}
