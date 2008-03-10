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

package org.apache.tapestry.internal.renderers;

import org.apache.tapestry.EventContext;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ioc.annotations.Primary;
import org.apache.tapestry.services.ObjectRenderer;

/**
 * Renders out the values stored inside a {@link EventContext}.
 */
public class EventContextRenderer implements ObjectRenderer<EventContext>
{
    private final ObjectRenderer _masterRenderer;

    public EventContextRenderer(@Primary ObjectRenderer masterRenderer)
    {
        _masterRenderer = masterRenderer;
    }


    public void render(EventContext object, MarkupWriter writer)
    {
        int count = object.getCount();

        if (count == 0) return;

        writer.element("ul", "class", "t-data-list");

        for (int i = 0; i < count; i++)
        {
            writer.element("li");

            _masterRenderer.render(object.get(Object.class, i), writer);

            writer.end();
        }

        writer.end();
    }
}
