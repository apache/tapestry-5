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

import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.PageRenderRequestParameters;

/**
 * Constant values for common event names fired by Tapestry components.
 */
public class EventConstants
{
    /**
     * Default client event name, "action", used in most situations.
     */
    public static final String ACTION = "action";

    /**
     * Event triggered when a page is activated (for rendering). The component event handler will be
     * passed the context
     * provided by the passivate event.
     */
    public static final String ACTIVATE = "activate";

    /**
     * Event triggered when a link for a page is generated. The event handler for the page may
     * provide an object, or an
     * array of objects, as the context for the page. These values will become part of the page's
     * context, and will be
     * provided back when the page is activated.
     */
    public static final String PASSIVATE = "passivate";

    /**
     * Invoked before {@link #PREPARE} when rendering out the form.
     *
     * @see org.apache.tapestry5.corelib.components.Form
     */
    public static final String PREPARE_FOR_RENDER = "prepareForRender";

    /**
     * Invoked before {@link #PREPARE} when the form is submitted.
     *
     * @see org.apache.tapestry5.corelib.components.Form
     */
    public static final String PREPARE_FOR_SUBMIT = "prepareForSubmit";

    /**
     * Invoked to let the containing component(s) prepare for the form rendering or the form
     * submission.
     *
     * @see org.apache.tapestry5.corelib.components.Form
     */
    public static final String PREPARE = "prepare";

    /**
     * Event type for a notification after the form has submitted. This event notification occurs on
     * any form submit,
     * without respect to "success" or "failure".
     *
     * @see org.apache.tapestry5.corelib.components.Form
     */
    public static final String SUBMIT = "submit";

    /**
     * Event triggered when a client-side form is canceled. This occurs after page activation, and after
     * the prepare events for the form, but before any stored {@link ComponentAction}s for the form are triggered.
     *
     * @see org.apache.tapestry5.corelib.SubmitMode#CANCEL
     *
     * @since 5.2.0
     */
    public static final String CANCELED = "canceled";

    /**
     * Event type for a notification after the form has submitted, when there are no errors in the
     * validation tracker.
     * This occurs before the {@link #SUBMIT} event.
     *
     * @see org.apache.tapestry5.corelib.components.Form
     */
    public static final String SUCCESS = "success";

    /**
     * Event type for a notification after the form has been submitted, when there are errors in the
     * validation tracker.
     * This occurs before the {@link #SUBMIT} event.
     */
    public static final String FAILURE = "failure";

    /**
     * Event type triggered by the {@link org.apache.tapestry5.corelib.components.Submit} component
     * when it is the cause
     * of the form submission.
     */
    public static final String SELECTED = "selected";

    /**
     * Event triggered by some form-related components to parse a value provided by the client. This
     * takes the place of a {@link org.apache.tapestry5.Translator}.
     */
    public static final String PARSE_CLIENT = "parseClient";

    /**
     * Event triggered by some form-related components to convert a server-side value to a
     * client-side string, as an
     * alternative to a {@link org.apache.tapestry5.Translator}.
     */
    public static final String TO_CLIENT = "toClient";

    /**
     * Event triggered by form-related components to validate user input. In addition, the Form component
     * fires a validate event just before it fires {@link #SUCCESS} or {@link #FAILURE} as a chance
     * to perform cross-form validations.
     */
    public static final String VALIDATE = "validate";

    /**
     * Event triggered by {@link org.apache.tapestry5.corelib.components.AjaxFormLoop} to inform the
     * container about the
     * row removed on the client side. The event context is the object that was removed.
     */
    public static final String REMOVE_ROW = "removeRow";

    /**
     * Event triggered by {@link org.apache.tapestry5.corelib.components.AjaxFormLoop} to inform the
     * container that a new row has been requested. The return value from the event handler must be the newly created
     * object, which must
     * also be visible in the {@link org.apache.tapestry5.corelib.components.AjaxFormLoop#encoder} parameter.
     */
    public static final String ADD_ROW = "addRow";

    /**
     * Event triggered by the {@link org.apache.tapestry5.corelib.components.Loop} component to
     * inform its container of
     * all the values that were supplied from the client during a form submission. The event handler
     * method should have
     * a single parameter, of type Object[] or type List, to receive the values.
     *
     * @since 5.1.0.0
     */
    public static final String SYNCHRONIZE_VALUES = "synchronizeValues";

    /**
     * Event triggered by {@link org.apache.tapestry5.corelib.components.ProgressiveDisplay} component to inform its
     * container of what context (if any) is available. The event handler may return a renderable
     * object or null. If
     * null is returned, the component renders its own body as the partial markup response.
     *
     * @since 5.1.0.1
     */
    public static final String PROGRESSIVE_DISPLAY = "progressiveDisplay";

    /**
     * Event triggered by an {@link org.apache.tapestry5.corelib.mixins.Autocomplete} mixin to
     * request completions of
     * the current input. The first value in the context is the input string; additional values
     * in the context parameter of the {@link org.apache.tapestry5.corelib.mixins.Autocomplete}
     * component.
     *
     * @since 5.1.0.4
     */
    public static final String PROVIDE_COMPLETIONS = "provideCompletions";

    /**
     * Event triggered by {@link org.apache.tapestry5.corelib.components.Select} component to inform
     * its
     * container that Select's value has changed.
     *
     * @since 5.2.0
     */
    public static final String VALUE_CHANGED = "valueChanged";

    /**
     * Event triggered during page render link generation. The first context parameter is the {@link Link} object, the
     * second is the {@link PageRenderRequestParameters} from which the Link
     * was created. The event is triggered on the actively rendering page.
     *
     * @since 5.2.0
     */
    public static final String DECORATE_PAGE_RENDER_LINK = "decoratePageRenderLink";

    /**
     * Event triggered during component event link generation. The first context parameter is the {@link Link} object,
     * the second is the {@link ComponentEventRequestParameters} from which the Link
     * was created. The event is triggered on the actively rendering page, not necessarily the page
     * containing the component.
     *
     * @since 5.2.0
     */
    public static final String DECORATE_COMPONENT_EVENT_LINK = "decorateComponentEventLink";

    /**
     * Name of a event triggered by the form component on the {@linkplain ComponentSource#getActivePage() active page}
     * to allow it to pre-allocate the names of any query parameters that might be used by the page for its own purposes
     * and should not be allocated to components. An {@link IdAllocator} is passed as the event context.
     *
     * @since 5.2.0
     */
    public static final String PREALLOCATE_FORM_CONTROL_NAMES = "preallocateFormControlNames";

    /**
     * Event  triggered by the {@link org.apache.tapestry5.corelib.components.Tree}
     * component when a leaf node is selected.
     *
     * @since 5.3
     */
    public static final String NODE_SELECTED = "nodeSelected";

    /**
     * Event  triggered by the {@link org.apache.tapestry5.corelib.components.Tree}
     * component when a leaf node is unselected.
     *
     * @since 5.3
     */
    public static final String NODE_UNSELECTED = "nodeUnselected";

    /**
     * Event triggered by {@link org.apache.tapestry5.corelib.mixins.ZoneRefresh ZoneRefresh} to refresh the
     * {@link org.apache.tapestry5.corelib.components.Zone Zone}
     *
     * @since 5.3
     */
    public static final String REFRESH = "refresh";
}
