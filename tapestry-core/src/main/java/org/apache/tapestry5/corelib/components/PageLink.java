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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.commons.util.CommonsUtils;
import org.apache.tapestry5.corelib.base.AbstractLink;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

/**
 * Generates a render request link to some other page in the application. If an activation context is supplied (as the
 * context parameter), then the context values will be encoded into the URL. If no context is supplied, then the target
 * page itself will supply the context via a passivate event.
 *
 * Pages are not required to have an activation context. When a page does have an activation context, the value
 * typically represents the identity of some object displayed or otherwise manipulated by the page.
 *
 * @tapestrydoc
 */
public class PageLink extends AbstractLink
{
    /**
     * The page to link to. If a <code>String</code>, as usual, it should be the page logical name.
     * If it's a <code>Class</code> instance, it's treated as the target page. 
     * If it's not a <code>String</code> nor an <code>Class</code>, the target page will be
     * the result of calling <code>page.getClass()</code>.
     * Notice you'll need to use the <code>prop</code> binding when passing a value which
     * isn't a <code>String</code>. 
     */
    @Parameter(required = true, allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private Object page;

    /**
     * If provided, this is the activation context for the target page (the information will be encoded into the URL).
     * If not provided, then the target page will provide its own activation context.
     */
    @Parameter
    private Object[] context;

    @Inject
    private PageRenderLinkSource linkSource;

    void beginRender(MarkupWriter writer)
    {
        if (isDisabled()) return;

        Link link;
        if (page instanceof String) {
            final String pageName = (String) page; 
            link = resources.isBound("context")
                ? linkSource.createPageRenderLinkWithContext(pageName, context == null ? CommonsUtils.EMPTY_STRING_ARRAY : context)
                : linkSource.createPageRenderLink(pageName);
        }
        else {
            // If page is a Class, use it directly. If not, use its class (type)
            Class<?> clasz = page instanceof Class<?> ? (Class<?>) page : page.getClass();
            link = resources.isBound("context")
                    ? linkSource.createPageRenderLinkWithContext(clasz, context == null ? CommonsUtils.EMPTY_STRING_ARRAY : context)
                    : linkSource.createPageRenderLink(clasz);
        }

        writeLink(writer, link);
    }

    void afterRender(MarkupWriter writer)
    {
        if (isDisabled()) return;

        writer.end(); // <a>
    }
}
