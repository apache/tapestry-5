package org.apache.tapestry5;

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
     * Event triggered when a page is activated (for rendering). The component event handler will be passed the context
     * provided by the passivate event.
     */
    public static final String ACTIVATE = "activate";

    /**
     * Event triggered when a link for a page is generated. The event handler for the page may provide an object, or an
     * array of objects, as the context for the page. These values will become part of the page's context, and will be
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
     * Invoked to let the containing component(s) prepare for the form rendering or the form submission.
     *
     * @see org.apache.tapestry5.corelib.components.Form
     */
    public static final String PREPARE = "prepare";

    /**
     * Event type for a notification after the form has submitted. This event notification occurs on any form submit,
     * without respect to "success" or "failure".
     *
     * @see org.apache.tapestry5.corelib.components.Form
     */
    public static final String SUBMIT = "submit";

    /**
     * Event type for a notification to perform validation of submitted data. This allows a listener to perform
     * cross-field validation. This occurs before the {@link #SUCCESS} or {@link #FAILURE} notification.
     *
     * @see org.apache.tapestry5.corelib.components.Form
     */
    public static final String VALIDATE_FORM = "validateForm";

    /**
     * Event type for a notification after the form has submitted, when there are no errors in the validation tracker.
     * This occurs before the {@link #SUBMIT} event.
     *
     * @see org.apache.tapestry5.corelib.components.Form
     */
    public static final String SUCCESS = "success";

    /**
     * Event type for a notification after the form has been submitted, when there are errors in the validation tracker.
     * This occurs before the {@link #SUBMIT} event.
     */
    public static final String FAILURE = "failure";

    /**
     * Event type triggered by the {@link org.apache.tapestry5.corelib.components.Submit} component when it is the cause
     * of the form submission.
     */
    public static final String SELECTED = "selected";

    /**
     * Event triggered by some form-related cmponents to parse a value provided by the client. This takes the place of a
     * {@link org.apache.tapestry5.Translator}.
     */
    public static final String PARSE_CLIENT = "parseClient";

    /**
     * Event triggered by some form-related components to convert a server-side value to a client-side string, as an
     * alternative to a {@link org.apache.tapestry5.Translator}.
     */
    public static final String TO_CLIENT = "toClient";

    /**
     * Event triggered by form-related components to validate user input.
     */
    public static final String VALIDATE = "validate";
}
