// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry.Asset;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.annotations.AfterRender;
import org.apache.tapestry.annotations.BeforeRenderBody;
import org.apache.tapestry.annotations.BeginRender;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;

/**
 * Renders an HTML img element using a supplied {@link Asset} as the image source. This is useful
 * when image to render is packaged within a component library JAR, or when the image to display is
 * computed dynamically.
 * <p>
 * Renders an id attribute (a {@link PageRenderSupport#allocateClientId(String) uniqued} version of
 * the component's id) as well as informal parameters.
 */
@SupportsInformalParameters
public class Img
{
    @Environmental
    private PageRenderSupport _support;

    @Inject
    private ComponentResources _resources;

    /**
     * The image asset to render.
     */
    @Parameter(required = true)
    private Asset _src;

    @BeginRender
    void begin(MarkupWriter writer)
    {
        String clientId = _support.allocateClientId(_resources.getId());

        writer.element("img", "src", _src, "id", clientId);

        _resources.renderInformalParameters(writer);
    }

    @BeforeRenderBody
    boolean beforeRenderBody()
    {
        return false;
    }

    @AfterRender
    void after(MarkupWriter writer)
    {
        writer.end();
    }
}
