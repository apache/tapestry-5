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

import org.apache.tapestry5.annotations.PublishEvent;
import org.apache.tapestry5.internal.structure.PageResetListener;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;

/**
 * Constants needed by end-user classes.
 *
 * @since 5.2.0
 */
public class TapestryConstants
{

    /**
     * The extension used for Tapestry component template files, <em>T</em>apestry <em>M</em>arkup <em>L</em>anguage.
     * Template files are well-formed XML files.
     */
    public static final String TEMPLATE_EXTENSION = "tml";

    /**
     * Name of query parameter that is placed on "loopback" links (page render links for the same
     * page). This mostly includes the redirects sent after a component event request. Page render
     * requests
     * that do <em>not</em> have the LOOPBACK query parameter will trigger a {@linkplain PageResetListener reset
     * notification} after the initialization event; the LOOPBACK prevents this reset notification.
     *
     * @see ComponentEventLinkEncoder#createPageRenderLink(org.apache.tapestry5.services.PageRenderRequestParameters)
     * @see ComponentEventLinkEncoder#decodePageRenderRequest(org.apache.tapestry5.http.services.Request)
     * @see PageResetListener
     * @since 5.2.0
     */
    public static final String PAGE_LOOPBACK_PARAMETER_NAME = "t:lb";

    /**
     * Name of a request attribute that contains an {@link org.apache.tapestry5.ioc.IOOperation}
     * used to render the response. The operation should return void.
     *
     * Implementations of {@link org.apache.tapestry5.services.ComponentEventResultProcessor}
     * will store a response rendering operation into the request; the operation, if present,
     * will be executed as the first filter inside the
     * {@link org.apache.tapestry5.services.ComponentRequestHandler} pipeline.
     *
     * This approach is recommended for any "complex" rendering that involves components or pages.
     * It is optional for other types.
     *
     * @since 5.4
     */
    public static final String RESPONSE_RENDERER = "tapestry.response-renderer";

    /**
     * Name of a {@link org.apache.tapestry5.http.services.Request} attribute, used
     * to disable JavaScript minimization during asset requests.
     *
     * @see org.apache.tapestry5.services.javascript.JavaScriptStack#getJavaScriptAggregationStrategy()
     * @since 5.4
     */
    public static final String DISABLE_JAVASCRIPT_MINIMIZATION = "tapestry.disable-javascript-minimization";

    /**
     * Name of the HTML data attribute which contains information about component events
     * published by using the {@linkplain PublishEvent} annotation
     * in a component event handler method.
     * 
     * @see PublishEvent
     * @since 5.4.2
     */
    public static final String COMPONENT_EVENTS_ATTRIBUTE_NAME = "data-component-events";

}
