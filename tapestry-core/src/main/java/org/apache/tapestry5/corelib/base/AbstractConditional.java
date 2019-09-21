// Copyright 2009-2013 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.base;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * Base class for {@link org.apache.tapestry5.corelib.components.If} and {@link org.apache.tapestry5.corelib.components.Unless}.
 * Will render its body or the block from its else parameter.  If it renders anything and it has an element name, then
 * it renders the element and its informal parameters.
 *
 * @tapestrydoc
 */
@SupportsInformalParameters
public abstract class AbstractConditional
{
    @Inject
    private ComponentResources resources;

    /**
     * Performs the test via the parameters; return true to render the body of the component, false to render the else
     * block (or nothing).
     *
     * @return true to render body
     */
    protected abstract boolean test();

    /**
     * The {@link org.apache.tapestry5.Block} to render if {@link #test()} is true. The default, null, means
     * render the component body in that situation..
     */
    @Parameter(name = "then", defaultPrefix = BindingConstants.LITERAL)
    private Block thenBlock;

    /**
     * An alternate {@link org.apache.tapestry5.Block} to render if {@link #test()} is false. The default, null, means
     * render nothing in that situation.
     */
    @Parameter(name = "else", defaultPrefix = BindingConstants.LITERAL)
    private Block elseBlock;

    private boolean renderTag;

    /**
     * Returns null if the {@link #test()} is true, which allows normal rendering (of the body). If the test parameter
     * is false, returns the else parameter (this may also be null).
     */
    Object beginRender(MarkupWriter writer)
    {
        boolean enabled = test();

        Block toRender = enabled ? (thenBlock == null ? resources.getBody() : thenBlock) : elseBlock;

        String elementName = resources.getElementName();

        if (enabled && elementName != null)
        {
            renderTag = true;
            writer.element(elementName);
            resources.renderInformalParameters(writer);
        }

        return toRender;
    }

    /**
     * If {@link #test()} is true, then the body is rendered, otherwise not. The component does not have a template or
     * do any other rendering besides its body (and possibly an element around its body).
     */
    boolean beforeRenderBody()
    {
        return false;
    }

    void afterRender(MarkupWriter writer)
    {
        if (renderTag)
        {
            writer.end();
            renderTag = false;
        }
    }


}
