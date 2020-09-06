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

package org.apache.tapestry5;

import org.apache.tapestry5.commons.util.CommonsUtils;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.annotations.ImmutableSessionPersistedObject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.PageRenderLinkSource;

import java.io.Serializable;

/**
 * A way of capturing the name of a page and the page activation context so that, at a future date,
 * the page can be invoked with that data. This kind of callback is very useful when creating more
 * complicated workflows, where access to a page is "interrupted" with some operation before
 * returning (via a callback) to the original flow.
 *
 * Since the callback is serializable, it can be stored in the session.
 * 
 * @since 5.2.0
 */
@ImmutableSessionPersistedObject
public class PageCallback implements Serializable
{
    private static final long serialVersionUID = -8067619978636824702L;

    private String pageName;

    private String[] activationContext;

    public PageCallback(String pageName, String[] activationContext)
    {
        assert InternalUtils.isNonBlank(pageName);
        this.pageName = pageName;
        assert activationContext != null;
        this.activationContext = activationContext;
    }

    public PageCallback(String pageName, EventContext activationContext)
    {
        this(pageName, activationContext.toStrings());
    }

    public PageCallback(String pageName)
    {
        this(pageName, CommonsUtils.EMPTY_STRING_ARRAY);
    }

    public String getPageName()
    {
        return pageName;
    }

    @Override
    public String toString()
    {
        if (hasActivationContext())
            return String.format("PageCallback[%s %s]", pageName, activationContextDescription());

        return String.format("PageCallback[%s]", pageName);
    }

    /** Does the activation context have any values? Used, typically, inside an override of {@link #toString()}. */
    protected final boolean hasActivationContext()
    {
        return activationContext.length > 0;
    }

    /**
     * Returns the activation context as a string of value separated by slashes. Typically used inside
     * an override of {@link #toString()}.
     */
    protected final String activationContextDescription()
    {
        StringBuilder builder = new StringBuilder();

        String sep = "";

        for (String c : activationContext)
        {
            builder.append(sep);
            builder.append(c);

            sep = "/";
        }

        return builder.toString();
    }

    /**
     * Converts the callback (the page name and activation context) to a link; such a link may be
     * returned from a event handler method to cause Tapestry to redirect to the page. Most of the
     * details
     * are encapsulated inside the {@link PageRenderLinkSource} service.
     * 
     * @param linkSource
     *            used to generate the link
     * @return link corresponding to this callback
     */
    public Link toLink(PageRenderLinkSource linkSource)
    {
        return linkSource.createPageRenderLinkWithContext(pageName, (Object[]) activationContext);
    }
}
