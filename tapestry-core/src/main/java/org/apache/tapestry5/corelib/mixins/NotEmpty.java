// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.dom.Element;

/**
 * A mixin that attaches to an element that renders an element.  At the end of the render, if the element is empty, then
 * a non-breaking space (&amp;nbsp;) is injected into the element. This is often necessary for proper rendering on the
 * client.
 * <p/>
 * Often used in conjunction with the {@link org.apache.tapestry5.corelib.components.Any} component.
 *
 * @since 5.1.0.0
 */
@MixinAfter
public class NotEmpty
{
    private Element element;

    void beginRender(MarkupWriter writer)
    {
        element = writer.getElement();
    }

    void afterRender()
    {
        if (element.isEmpty())
        {
            element.removeChildren();
            element.raw("&nbsp;");
        }
    }
}
