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

package org.apache.tapestry5.services;

/**
 * An event handler method may return an instance of this class to trigger the rendering
 * of a particular page without causing a redirect to that page; the rendering takes place as part
 * of the original component event request, thus forming the opposite of Tapestry's normal
 * redirect-after-event behavior.
 *
 * The page will be activated using the provided page activation context (or an empty page activation
 * context). Starting with 5.3, the page activation step can be bypassed. Rendering occurs using
 * the standard {@link PageRenderRequestHandler} pipeline.
 *
 *
 * @since 5.2.0
 */
public final class StreamPageContent
{
    private final Class<?> pageClass;

    private final Object[] pageActivationContext;

    private final boolean bypassActivation;

    /**
     * Creates an instance that streams the activate page's content (that is, {@link #getPageClass()} will be null).
     * Unless otherwise configured, page activation will take place.
     *
     * @since 5.4
     */
    public StreamPageContent()
    {
        this(null);
    }

    /**
     * Renders the page using an empty page activation context.
     *
     * @param pageClass class of the page to render
     */
    public StreamPageContent(final Class<?> pageClass)
    {
        this(pageClass, (Object[]) null);
    }

    /**
     * Renders the page using the supplied page activation context.
     *
     * @param pageClass             class of the page to render, or null to render the currently active page (as per
     *                              {@link org.apache.tapestry5.http.services.RequestGlobals#getActivePageName()})
     * @param pageActivationContext activation context of the page
     */
    public StreamPageContent(final Class<?> pageClass, final Object... pageActivationContext)
    {
        this(pageClass, pageActivationContext, false);
    }

    private StreamPageContent(Class<?> pageClass, Object[] pageActivationContext, boolean bypassActivation)
    {
        this.pageClass = pageClass;
        this.pageActivationContext = pageActivationContext;
        this.bypassActivation = bypassActivation;
    }

    /**
     * Returns the class of the page to render, or null to indicate that the active page for the request should simply
     * be re-rendered.
     */
    public Class<?> getPageClass()
    {
        return this.pageClass;
    }

    /**
     * Returns the activation context of the page. May return null to indicate an empty activation context.
     */
    public Object[] getPageActivationContext()
    {
        return this.pageActivationContext;
    }

    /**
     * Returns a new StreamPageInstance with the {@linkplain #isBypassActivation bypass activation flag} set to true, such that
     * page activation will be bypassed when the page is rendered.
     *
     * @return new instance
     */
    public StreamPageContent withoutActivation()
    {
        if (pageActivationContext != null)
        {
            throw new IllegalStateException("A StreamPageContext instance created with a page activation context may not be converted to bypass page activation.");
        }

        return new StreamPageContent(pageClass, null, true);
    }

    /**
     * @return true if configured to bypass activation
     */
    public boolean isBypassActivation()
    {
        return bypassActivation;
    }
}
