// Copyright 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Triggers an arbitrary event during rendering. This is often useful to add JavaScript
 * to a page or a component (via calls to the {@link JavaScriptSupport} environmental).
 * 
 * @since 5.2.0
 * @tapestrydoc
 */
public class Trigger
{
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String event;

    @Inject
    private ComponentResources resources;

    String defaultEvent()
    {
        return this.resources.getId();
    }

    boolean beginRender(MarkupWriter writer)
    {
        this.resources.triggerEvent(this.event, new Object[]
        { writer }, null);
        return false;
    }

}
