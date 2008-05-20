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

package org.apache.tapestry5.corelib.mixins;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * Used to render out all informal parameters, at the end of the {@link org.apache.tapestry5.annotations.BeginRender}
 * phase.
 * <p/>
 * This mixin can be used with components that render a single tag inside the {@link BeginRender} phase. RenderInformals
 * will activate during the PostBeginRender phase to write additional attributes, from the informal parameters, into the
 * active element.
 * <p/>
 * If you want this behavior, but need to render more than a single tag, then implement render phase methods for the
 * {@link BeforeRenderTemplate} and {@link AfterRenderTemplate} phases. Use those phases to write the additional
 * elements and close them.
 * <p/>
 * This is often used as a base class, for cases where a component doesn't have other mixins.
 */
@MixinAfter
@SupportsInformalParameters
public class RenderInformals
{
    @Inject
    private ComponentResources resources;

    void beginRender(MarkupWriter writer)
    {
        resources.renderInformalParameters(writer);
    }
}
