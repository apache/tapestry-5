// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.renderers;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.ObjectRenderer;

/**
 * Renders out the values stored inside a {@link EventContext}.
 */
public class EventContextRenderer implements ObjectRenderer<EventContext>
{
    private final ObjectRenderer masterRenderer;

    public EventContextRenderer(@Primary ObjectRenderer masterRenderer)
    {
        this.masterRenderer = masterRenderer;
    }


    public void render(EventContext object, MarkupWriter writer)
    {
        int count = object.getCount();

        if (count == 0) return;

        writer.element("ul", "class", "t-data-list");

        for (int i = 0; i < count; i++)
        {
            writer.element("li");

            masterRenderer.render(object.get(Object.class, i), writer);

            writer.end();
        }

        writer.end();
    }
}
