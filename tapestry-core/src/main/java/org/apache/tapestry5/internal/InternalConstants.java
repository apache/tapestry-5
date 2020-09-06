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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.annotations.PublishEvent;
import org.apache.tapestry5.commons.util.CommonsUtils;
import org.apache.tapestry5.commons.util.TimeInterval;
import org.apache.tapestry5.dom.MarkupModel;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.http.TapestryHttpConstants;
import org.apache.tapestry5.http.internal.TapestryHttpInternalConstants;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.javascript.JavaScriptStack;

public final class InternalConstants
{
    /**
     * Init parameter used to identify the package from which application classes are loaded. Such
     * classes are in the
     * pages, components and mixins sub-packages.
     * @deprecated Use {@link TapestryHttpInternalConstants#TAPESTRY_APP_PACKAGE_PARAM} instead
     */
    public static final String TAPESTRY_APP_PACKAGE_PARAM = TapestryHttpInternalConstants.TAPESTRY_APP_PACKAGE_PARAM;

    /**
     * Turns off loading of default modules (as driven by JAR file Manifest entries).
     * @deprecated Use {@link TapestryHttpInternalConstants#DISABLE_DEFAULT_MODULES_PARAM} instead
     */
    public static final String DISABLE_DEFAULT_MODULES_PARAM = TapestryHttpInternalConstants.DISABLE_DEFAULT_MODULES_PARAM;

    /**
     * The name of the query parameter that stores the page activation context inside an action
     * request.
     */
    public static final String PAGE_CONTEXT_NAME = "t:ac";

    /**
     * Name of event triggered by Grid sub-components when an in-place Grid is updated.
     */
    public static final String GRID_INPLACE_UPDATE = "inplaceupdate";

    /**
     * The name of a query parameter that stores the containing page (used in action links when the
     * page containing the
     * component is not the same as the page that was rendering). The active page (the page which
     * initiated the render)
     * is encoded into the URL, and the containing page is tacked on as this query parameter.
     */
    public static final String CONTAINER_PAGE_NAME = "t:cp";

    public static final String MIXINS_SUBPACKAGE = "mixins";

    public static final String COMPONENTS_SUBPACKAGE = "components";

    public static final String PAGES_SUBPACKAGE = "pages";

    public static final String BASE_SUBPACKAGE = "base";

    /**
     * Used in some Ajax scenarios to set the content type for the response early, when the Page
     * instance (the authority
     * on content types) is known. The value is of type {@link org.apache.tapestry5.http.ContentType}.
     */
    public static final String CONTENT_TYPE_ATTRIBUTE_NAME = "content-type";

    /**
     * @deprecated Use {@link TapestryHttpInternalConstants#CHARSET_CONTENT_TYPE_PARAMETER} instead
     */
    public static final String CHARSET_CONTENT_TYPE_PARAMETER = TapestryHttpInternalConstants.CHARSET_CONTENT_TYPE_PARAMETER;

    /**
     * As above but to store the name of the page. Necessary for determining the correct
     * {@link MarkupModel} for the response.
     */
    public static final String PAGE_NAME_ATTRIBUTE_NAME = "page-name";

    /**
     * Required MIME type for JSON responses. If this MIME type is not used, the client-side
     * Prototype code will not
     * recognize the response as JSON, and the Ajax.Response.responseJSON property will be null.
     */
    public static final String JSON_MIME_TYPE = "application/json";

    /**
     * Request attribute key; if non-null, then automatic GZIP compression of response stream is
     * suppressed. This is
     * useful when the code opening the response stream wants to explicitly control whether GZIP
     * compression occurs or
     * not. Alias to {@link TapestryHttpConstants#SUPPRESS_COMPRESSION}.
     *
     * @since 5.1.0.0
     */
    public static final String SUPPRESS_COMPRESSION = TapestryHttpConstants.SUPPRESS_COMPRESSION;

    /**
     * Name of response header for content encoding.
     *
     * @since 5.1.0.0
     * @deprecated Use {@link TapestryHttpInternalConstants#CONTENT_ENCODING_HEADER} instead
     */
    public static final String CONTENT_ENCODING_HEADER = TapestryHttpInternalConstants.CONTENT_ENCODING_HEADER;

    /**
     * Response content encoding value indicating use of GZIP compression.
     *
     * @since 5.1.0.0
     * @deprecated Use {@link TapestryHttpInternalConstants#GZIP_CONTENT_ENCODING} instead
     */
    public static final String GZIP_CONTENT_ENCODING = TapestryHttpInternalConstants.GZIP_CONTENT_ENCODING;

    /**
     * Identifies the start of an expansion inside a template.
     */
    public static final String EXPANSION_START = "${";

    /**
     * Special prefix for parameters that are inherited from named parameters of their container.
     */
    public static final String INHERIT_BINDING_PREFIX = "inherit:";

    public static final long TEN_YEARS = new TimeInterval("10y").milliseconds();

    /**
     * @deprecated Use {@link CommonsUtils#EMPTY_STRING_ARRAY} instead
     */
    public static final String[] EMPTY_STRING_ARRAY = CommonsUtils.EMPTY_STRING_ARRAY;

    /**
     * Name of the core {@link JavaScriptStack}, which supplies the basic JavaScript infrastructure
     * on the client.
     *
     * @since 5.2.0
     */
    public static final String CORE_STACK_NAME = "core";

    /**
     * Virtual folder name for the core library. The core library is special as any component not present in another
     * library (including the application library) is searched for inside core.
     *
     * @since 5.3
     */
    public static final String CORE_LIBRARY = "core";

    /**
     * The names of the standard controlled subpackages.
     *
     * @since 5.3
     */
    public static final String[] SUBPACKAGES =
            {PAGES_SUBPACKAGE, COMPONENTS_SUBPACKAGE, MIXINS_SUBPACKAGE, BASE_SUBPACKAGE};

    /**
     * The element name for a submit input element used to cancel the form (rather than
     * submit it normally).
     *
     * @since 5.3
     */
    public static final String CANCEL_NAME = "cancel";

    /**
     * Request attribute that can be set to bypass page activation.
     *
     * @see org.apache.tapestry5.internal.services.StreamPageContentResultProcessor
     * @see org.apache.tapestry5.internal.services.PageRenderRequestHandlerImpl
     * @since 5.4
     */
    public static final String BYPASS_ACTIVATION = "tapestry.bypass-page-activation";

    /**
     * Key inside the response that contains the partial page render keys that are used
     * to update the client.
     *
     * @since 5.4
     */
    public static final String PARTIAL_KEY = "_tapestry";

    /**
     * Request attribute, set to true once the active page (as identified in the incoming
     * component event or page render request) has been successfully loaded. This is very important
     * to the {@link org.apache.tapestry5.corelib.pages.ExceptionReport} page, which can possibly
     * fail (resulting in a servlet container 500 response) if the page can't be loaded (because
     * if the page can't be loaded, then a link to the page can't be created).
     *
     * @since 5.4
     */
    public static final String ACTIVE_PAGE_LOADED = "tapestry.active-page-loaded";

    /**
     * Used to suppress the stylesheets from the 'core' stack; this is used on certain pages
     * that want to work around application-specific overrides to the core stack stylesheets.
     *
     * @since 5.4
     */
    public static final String SUPPRESS_CORE_STYLESHEETS = "tapestry.suppress-core-stylesheets";

    /**
     * A bit of a hack that allows, in production mode, for a component event request to "unwind" when
     * the component referenced in the URL does not exist. This is related to TAP5-1481. This situation
     * can most likely occur when a web spider, such as Google, uses an old component event URI from
     * a prior deployment, which no longer works in a new deployment, due to structural changes. Since
     * changing the APIs that significantly is forbidden, a non-null value is added as an
     * {@link org.apache.tapestry5.http.services.Request} attribute.
     *
     * @since 5.4
     */
    public static final String REFERENCED_COMPONENT_NOT_FOUND = "tapestry.referenced-component-not-found";

    /**
     * Name of request parameter that suppresses the logic that injects a random-ish namespace into allocated ids when rending partial page
     * responses. This, of course, requires a lot of testing to ensure that there are no resulting name clashes,
     * and should not be used inside Zones containing an {@link org.apache.tapestry5.corelib.components.AjaxFormLoop}.
     *
     * @since 5.4
     */
    public static final String SUPPRESS_NAMESPACED_IDS = "t:suppress-namespaced-ids";

    /**
     * @since 5.4
     */
    public static final ContentType JAVASCRIPT_CONTENT_TYPE = new ContentType("text/javascript");
    
    /**
     * Name of the {@linkplain ComponentModel} metadata key whiche stores the {@linkplain PublishEvent}
     * data.
     * @since 5.4.2
     */
    public static final String PUBLISH_COMPONENT_EVENTS_META = "meta.publish-component-events";
    
    /**
     * Name of the JSONObject key name which holds the name of the event to be published.
     * 
     * @since 5.4.2
     */
    public static final String PUBLISH_COMPONENT_EVENTS_URL_PROPERTY = "url";

}
