// Copyright 2006, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.bindings;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.runtime.Event;

/**
 * For use in places where we don't want to have to transform a class just for testing purposes.
 */
public class DefaultComponent implements Component
{

    public void afterRender(MarkupWriter writer, Event event)
    {
    }

    public void afterRenderBody(MarkupWriter writer, Event event)
    {
    }

    public void afterRenderTemplate(MarkupWriter writer, Event event)
    {
    }

    public void beforeRenderBody(MarkupWriter writer, Event event)
    {
    }

    public void beforeRenderTemplate(MarkupWriter writer, Event event)
    {
    }

    public void beginRender(MarkupWriter writer, Event event)
    {
    }

    public void cleanupRender(MarkupWriter writer, Event event)
    {
    }

    public boolean dispatchComponentEvent(ComponentEvent event)
    {
        return false;
    }

    public void postRenderCleanup()
    {
    }

    public void setupRender(MarkupWriter writer, Event event)
    {
    }

    public ComponentResources getComponentResources()
    {
        return null;
    }

    public void containingPageDidAttach()
    {
    }

    public void containingPageDidDetach()
    {
    }

    public void containingPageDidLoad()
    {
    }

    public void restoreStateBeforePageAttach()
    {
    }
}
