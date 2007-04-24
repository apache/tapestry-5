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

package org.apache.tapestry.internal.services;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Link;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.PageLifecycleListener;

public class NoOpPage implements Page
{
    private final String _pageName;

    private final Locale _locale;

    public NoOpPage(String pageName, Locale locale)
    {
        _pageName = pageName;
        _locale = locale;
    }

    public String getName()
    {
        return _pageName;
    }

    public Locale getLocale()
    {
        return _locale;
    }

    public void setRootElement(ComponentPageElement component)
    {

    }

    public ComponentPageElement getRootElement()
    {
        return null;
    }

    public Component getRootComponent()
    {
        return null;
    }

    public boolean detached()
    {
        // It is not dirty.
        return false;
    }

    public void attached()
    {

    }

    public void loaded()
    {

    }

    public void addLifecycleListener(PageLifecycleListener listener)
    {

    }

    public Log getLog()
    {
        return null;
    }

    public ComponentPageElement getComponentElementByNestedId(String nestedId)
    {
        return null;
    }

    public Link createActionLink(ComponentPageElement element, String action, boolean forForm,
            Object... context)
    {
        return null;
    }

    public void persistFieldChange(ComponentResources resources, String fieldName, Object newValue)
    {
    }

    public Object getFieldChange(ComponentPageElement element, String fieldName)
    {
        return null;
    }

    public void incrementDirtyCount()
    {

    }

    public void decrementDirtyCount()
    {

    }

    public Link createPageLink(String pageName)
    {
        return null;
    }

}
