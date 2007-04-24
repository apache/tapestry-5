// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Link;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.AfterRender;
import org.apache.tapestry.annotations.BeginRender;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.services.PageRenderSupport;

/**
 * Generates a link to some other page in the application.
 */
@ComponentClass
public class PageLink
{
    @Parameter(required = true, defaultPrefix = "literal")
    private String _page;

    @Inject
    private ComponentResources _resources;

    @Environmental
    private PageRenderSupport _support;

    @BeginRender
    void begin(MarkupWriter writer)
    {
        String clientId = _support.allocateClientId(_resources.getId());

        Link link = _resources.createPageLink(_page);

        writer.element("a", "href", link, "id", clientId);

        _resources.renderInformalParameters(writer);
    }

    @AfterRender
    void end(MarkupWriter writer)
    {
        writer.end(); // <a>
    }
}
