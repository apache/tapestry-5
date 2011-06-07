// Copyright 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.mixins;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * This mixin triggers event notifcations to identify when it enters
 * the {@link BeginRender} and {@link AfterRender} render phases.
 * The {@link MarkupWriter} is passed as the event context. The most common use of this
 * is to handle the "afterRender" event to generate client-side JavaScript for content
 * just rendered via a {@link Block} (this is a common Ajax use case related to partial
 * page rendering).
 * 
 * @since 5.2.0
 * @tapestrydoc
 */
@Events(
{ "beginRender", "afterRender" })
public class RenderNotification
{
    @Inject
    private ComponentResources resources;

    void beginRender(MarkupWriter writer)
    {
        trigger(writer, "beginRender");
    }

    void afterRender(MarkupWriter writer)
    {
        trigger(writer, "afterRender");
    }

    private void trigger(MarkupWriter writer, String eventName)
    {
        resources.triggerEvent(eventName, new Object[]
        { writer }, null);
    }
}
