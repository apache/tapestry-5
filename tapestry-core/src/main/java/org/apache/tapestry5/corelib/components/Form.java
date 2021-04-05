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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.services.PropertyAccess;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.corelib.ClientValidation;
import org.apache.tapestry5.corelib.internal.ComponentActionSink;
import org.apache.tapestry5.corelib.internal.FormSupportImpl;
import org.apache.tapestry5.corelib.internal.InternalFormSupport;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.BeanValidationContext;
import org.apache.tapestry5.internal.BeanValidationContextImpl;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.FormControlNameManager;
import org.apache.tapestry5.internal.services.HeartbeatImpl;
import org.apache.tapestry5.internal.util.AutofocusValidationDecorator;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.StreamPageContent;
import org.apache.tapestry5.services.compatibility.DeprecationWarning;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * An HTML form, which will enclose other components to render out the various
 * types of fields.
 *
 * A Form triggers many notification events. When it renders, it triggers a
 * {@link org.apache.tapestry5.EventConstants#PREPARE_FOR_RENDER} notification, followed by a
 * {@link EventConstants#PREPARE} notification.
 *
 * When the form is submitted, the component triggers several notifications: first a
 * {@link EventConstants#PREPARE_FOR_SUBMIT}, then a {@link EventConstants#PREPARE}: these allow the page to update its
 * state as necessary to prepare for the form submission.
 *
 * The Form component then determines if the form was cancelled (see {@link org.apache.tapestry5.corelib.SubmitMode#CANCEL}). If so,
 * a {@link EventConstants#CANCELED} event is triggered.
 *
 * Next come notifications to contained components (or more accurately, the execution of stored {@link ComponentAction}s), to allow each component to retrieve and validate
 * submitted values, and update server-side properties.  This is based on the {@code t:formdata} query parameter,
 * which contains serialized object data (generated when the form initially renders).
 *
 * Once the form data is processed, the next step is to trigger the
 * {@link EventConstants#VALIDATE}, which allows for cross-form validation. After that, either a
 * {@link EventConstants#SUCCESS} OR {@link EventConstants#FAILURE} event (depending on whether the
 * {@link ValidationTracker} has recorded any errors). Lastly, a {@link EventConstants#SUBMIT} event, for any listeners
 * that care only about form submission, regardless of success or failure.
 *
 * For all of these notifications, the event context is derived from the <strong>context</strong> component parameter. This
 * context is encoded into the form's action URI (the parameter is not read when the form is submitted, instead the
 * values encoded into the form are used).
 *
 *
 * While rendering, or processing a Form submission, the Form component places a {@link FormSupport} object into the {@linkplain Environment environment},
 * so that enclosed components can coordinate with the Form component. It also places a {@link ValidationTracker} into the environment during both render and submission.
 * During submission it also pushes a {@link Heartbeat} into the environment, which is {@link org.apache.tapestry5.services.Heartbeat#end() ended} just before
 * {@linkplain FormSupport#defer(Runnable) deferred FormSupport operations} are executed.
 *
 *
 * @tapestrydoc
 * @see BeanEditForm
 * @see Errors
 * @see FormFragment
 * @see Label
 */
@Events(
        {EventConstants.PREPARE_FOR_RENDER, EventConstants.PREPARE, EventConstants.PREPARE_FOR_SUBMIT,
                EventConstants.VALIDATE, EventConstants.SUBMIT, EventConstants.FAILURE, EventConstants.SUCCESS, EventConstants.CANCELED})
@SupportsInformalParameters
public class Form implements ClientElement, FormValidationControl
{
    /**
     * Query parameter name storing form data (the serialized commands needed to
     * process a form submission).
     */
    public static final String FORM_DATA = "t:formdata";

    /**
     * Used by {@link Submit}, etc., to identify which particular client-side element (by element id)
     * was responsible for the submission. An empty hidden field is created, as needed, to store this value.
     * Starting in Tapestry 5.3, this is a JSONArray with two values: the client id followed by the client name.
     *
     * @since 5.2.0
     */
    public static final String SUBMITTING_ELEMENT_ID = "t:submit";
    
    /**
     * Name of the data attribute added to HTML forms generated by this component.
     * @since 5.6.4
     */
    public static final String DATA_ATTRIBUTE = "data-generator";
    
    /**
     * Name of the data attribute added to HTML forms generated by this component.
     * @since 5.6.4
     * @see #DATA_ATTRIBUTE
     */
    public static final String DATA_ATTRIBUTE_VALUE = "tapestry/core/form";

    public static final StreamPageContent STREAM_ACTIVE_PAGE_CONTENT = new StreamPageContent().withoutActivation();

    /**
     * The context for the link (optional parameter). This list of values will
     * be converted into strings and included in
     * the URI. The strings will be coerced back to whatever their values are
     * and made available to event handler
     * methods.
     */
    @Parameter
    private Object[] context;

    /**
     * The object which will record user input and validation errors. When not using
     * the default behavior supplied by the Form component (an immediate re-render of the active
     * page when there are form validation errors), it is necessary to bind this parameter
     * to a persistent value that can be maintained until the active page is re-rendered. See
     * <a href="https://issues.apache.org/jira/browse/TAP5-1808">TAP5-1801</a>.
     */
    @Parameter("defaultTracker")
    protected ValidationTracker tracker;

    @Inject
    @Symbol(SymbolConstants.FORM_CLIENT_LOGIC_ENABLED)
    private boolean clientLogicDefaultEnabled;

    /**
     * Controls when client validation occurs on the client, if at all. Defaults to {@link ClientValidation#SUBMIT}.
     * {@link ClientValidation#BLUR} was the default, prior to Tapestry 5.4, but is no longer supported.
     */
    @Parameter(allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private ClientValidation clientValidation = clientLogicDefaultEnabled ? ClientValidation.SUBMIT
            : ClientValidation.NONE;

    /**
     * If true (the default), then the JavaScript will be added to position the
     * cursor into the form. The field to
     * receive focus is the first rendered field that is in error, or required,
     * or present (in that order of priority).
     *
     * @see SymbolConstants#FORM_CLIENT_LOGIC_ENABLED
     */
    @Parameter
    private boolean autofocus = clientLogicDefaultEnabled;

    /**
     * Binding the zone parameter will cause the form submission to be handled
     * as an Ajax request that updates the
     * indicated zone. Often a Form will update the same zone that contains it.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String zone;

    /**
     * If true, then the Form's action will be secure (using an absolute URL with the HTTPs scheme) regardless
     * of whether the containing page itself is secure or not. This parameter does nothing
     * when {@linkplain SymbolConstants#SECURE_ENABLED security is disabled} (which is often
     * the case in development mode). This only affects how the Form's action attribute is rendered, there is
     * not (currently) a check that the form is actually submitted securely.
     */
    @Parameter
    private boolean secure;

    /**
     * Prefix value used when searching for validation messages and constraints.
     * The default is the Form component's
     * id. This is overridden by {@link org.apache.tapestry5.corelib.components.BeanEditForm}.
     *
     * @see org.apache.tapestry5.services.FormSupport#getFormValidationId()
     */
    @Parameter
    private String validationId;

    /**
     * Object to validate during the form submission process. The default is the Form component's container.
     * This parameter should only be used in combination with the Bean Validation Library.
     */
    @Parameter
    private Object validate;

    /**
     * When true, the the form will submit as an asynchronous request (via XmlHttpRequest); the event handler methods
     * can make use of the {@link org.apache.tapestry5.services.ajax.AjaxResponseRenderer} in order to force content
     * updates to the client.  This is used as an alternative to placing the form inside a {@link org.apache.tapestry5.corelib.components.Zone}
     * and binding the {@code zone} parameter.
     *
     * @since 5.4
     */
    @Parameter
    private boolean async = false;

    @Inject
    private Logger logger;

    @Inject
    private Environment environment;

    @Inject
    private ComponentResources resources;

    @Inject
    private Messages messages;

    @Environmental
    private JavaScriptSupport javascriptSupport;

    @Inject
    private Request request;

    @Inject
    private ComponentSource source;

    @Inject
    private FormControlNameManager formControlNameManager;


    /**
     * Starting in 5.4, this is a simple, non-persistent property, with no extra magic tricks.
     */
    private ValidationTracker defaultTracker;

    @Inject
    @Symbol(SymbolConstants.SECURE_ENABLED)
    private boolean secureEnabled;

    private InternalFormSupport formSupport;

    private Element form;

    private Element div;

    // Collects a stream of component actions. Each action goes in as a UTF
    // string (the component
    // component id), followed by a ComponentAction

    private ComponentActionSink actionSink;

    @SuppressWarnings("unchecked")
    @Environmental
    private TrackableComponentEventCallback eventCallback;

    @Inject
    private ClientDataEncoder clientDataEncoder;

    @Inject
    private PropertyAccess propertyAccess;

    @Inject
    private DeprecationWarning deprecationWarning;

    private String clientId;

    @Inject
    private ComponentSource componentSource;

    String defaultValidationId()
    {
        return resources.getId();
    }

    Object defaultValidate()
    {
        return resources.getContainer();
    }

    /**
     * Returns an instance of {@link ValidationTrackerImpl}, lazily creating it as needed. This property
     * is the default for the <strong>tracker</strong> parameter; the property (as of Tapestry 5.4) is not
     * persistent.
     *
     * @return per-request cached instance
     */
    public ValidationTracker getDefaultTracker()
    {
        if (defaultTracker == null)
        {
            defaultTracker = new ValidationTrackerImpl();
        }

        return defaultTracker;
    }

    /**
     * @deprecated In 5.4; previously used only for testing
     */
    public void setDefaultTracker(ValidationTracker defaultTracker)
    {
        this.defaultTracker = defaultTracker;
    }

    void setupRender()
    {
        FormSupport existing = environment.peek(FormSupport.class);

        if (existing != null)
        {
            throw new TapestryException(messages.get("core-form-nesting-not-allowed"), existing, null);
        }

        if (clientValidation == ClientValidation.BLUR)
        {
            deprecationWarning.componentParameterValue(resources, "clientValidation", clientValidation, "BLUR is no longer supported, starting in 5.4. Validation will occur as with SUBMIT.");
        }
    }

    void beginRender(MarkupWriter writer)
    {
        Link link = resources.createFormEventLink(EventConstants.ACTION, context);

        String actionURL = secure && secureEnabled ? link.toAbsoluteURI(true) : link.toURI();

        actionSink = new ComponentActionSink(logger, clientDataEncoder);

        clientId = javascriptSupport.allocateClientId(resources);

        // Pre-register some names, to prevent client-side collisions with function names
        // attached to the JS Form object.

        IdAllocator allocator = new IdAllocator();

        preallocateNames(allocator);

        formSupport = createRenderTimeFormSupport(clientId, actionSink, allocator);

        environment.push(FormSupport.class, formSupport);
        environment.push(ValidationTracker.class, tracker);

        if (autofocus)
        {
            ValidationDecorator autofocusDecorator = new AutofocusValidationDecorator(
                    environment.peek(ValidationDecorator.class), tracker, javascriptSupport);
            environment.push(ValidationDecorator.class, autofocusDecorator);
        }

        // Now that the environment is setup, inform the component or other
        // listeners that the form
        // is about to render.

        resources.triggerEvent(EventConstants.PREPARE_FOR_RENDER, context, null);

        resources.triggerEvent(EventConstants.PREPARE, context, null);

        // Push BeanValidationContext only after the container had a chance to prepare
        environment.push(BeanValidationContext.class, new BeanValidationContextImpl(validate));

        // Save the form element for later, in case we want to write an encoding
        // type attribute.

        form = writer.element("form",
                "id", clientId,
                "method", "post",
                "action", actionURL,
                "data-update-zone", zone,
                DATA_ATTRIBUTE, DATA_ATTRIBUTE_VALUE);

        if (clientValidation != ClientValidation.NONE)
        {
            writer.attributes("data-validate", "submit");
        }

        if (async)
        {
            javascriptSupport.require("t5/core/zone");
            writer.attributes("data-async-trigger", true);
        }

        resources.renderInformalParameters(writer);

        div = writer.element("div");

        for (String parameterName : link.getParameterNames())
        {
            String[] values = link.getParameterValues(parameterName);

            for (String value : values)
            {
                // The parameter value is expected to be encoded,
                // but the input value shouldn't be encoded.
                try
                {
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (UnsupportedEncodingException e)
                {
                    logger.error("Enable to decode parameter value for parameter {} in form {}",
                            parameterName, form.getName(), e);
                }
                writer.element("input", "type", "hidden", "name", parameterName, "value", value);
                writer.end();
            }
        }

        writer.end(); // div

        environment.peek(Heartbeat.class).begin();
    }

    /**
     * Creates an {@link org.apache.tapestry5.corelib.internal.InternalFormSupport} for
     * this Form.
     *
     * This method may also be invoked as the handler for the "internalCreateRenderTimeFormSupport" event.
     *
     * @param clientId
     *         the client-side id for the rendered form
     *         element
     * @param actionSink
     *         used to collect component actions that will, ultimately, be
     *         written as the t:formdata hidden
     *         field
     * @param allocator
     *         used to allocate unique ids
     * @return form support object
     */
    @OnEvent("internalCreateRenderTimeFormSupport")
    InternalFormSupport createRenderTimeFormSupport(String clientId, ComponentActionSink actionSink,
                                                    IdAllocator allocator)
    {
        return new FormSupportImpl(resources, clientId, actionSink,
                clientValidation != ClientValidation.NONE, allocator, validationId);
    }

    void afterRender(MarkupWriter writer)
    {
        environment.peek(Heartbeat.class).end();

        formSupport.executeDeferred();

        String encodingType = formSupport.getEncodingType();

        if (encodingType != null)
        {
            form.forceAttributes("enctype", encodingType);
        }

        writer.end(); // form

        div.element("input", "type", "hidden", "name", FORM_DATA, "value", actionSink.getClientData());
        div.pop();

        if (autofocus)
        {
            environment.pop(ValidationDecorator.class);
        }
    }

    void cleanupRender()
    {
        environment.pop(FormSupport.class);

        formSupport = null;

        environment.pop(ValidationTracker.class);

        tracker.clear();

        environment.pop(BeanValidationContext.class);
    }

    @SuppressWarnings(
            {"unchecked", "InfiniteLoopStatement"})
    Object onAction(EventContext context) throws IOException
    {
        beforeProcessSubmit(context);

        tracker.clear();

        formSupport = new FormSupportImpl(resources, validationId);

        environment.push(ValidationTracker.class, tracker);
        environment.push(FormSupport.class, formSupport);

        Heartbeat heartbeat = new HeartbeatImpl();

        environment.push(Heartbeat.class, heartbeat);

        heartbeat.begin();

        boolean didPushBeanValidationContext = false;

        try
        {
            resources.triggerContextEvent(EventConstants.PREPARE_FOR_SUBMIT, context, eventCallback);

            if (eventCallback.isAborted())
                return true;

            resources.triggerContextEvent(EventConstants.PREPARE, context, eventCallback);
            if (eventCallback.isAborted())
                return true;

            if (isFormCancelled())
            {
                executeStoredActions(true);

                resources.triggerContextEvent(EventConstants.CANCELED, context, eventCallback);
                if (eventCallback.isAborted())
                    return true;
            }

            environment.push(BeanValidationContext.class, new BeanValidationContextImpl(validate));

            didPushBeanValidationContext = true;

            executeStoredActions(false);

            heartbeat.end();

            formSupport.executeDeferred();

            fireValidateEvent(EventConstants.VALIDATE, context, eventCallback);

            if (eventCallback.isAborted())
            {
                return true;
            }

            afterValidate();

            // Let the listeners know about overall success or failure. Most
            // listeners fall into
            // one of those two camps.

            // If the tracker has no errors, then clear it of any input values
            // as well, so that the next page render will be "clean" and show
            // true persistent data, not value from the previous form
            // submission.

            if (!tracker.getHasErrors())
            {
                tracker.clear();
            }

            String eventType = tracker.getHasErrors()
                    ? EventConstants.FAILURE
                    : EventConstants.SUCCESS;

            resources.triggerContextEvent(eventType, context, eventCallback);

            if (eventCallback.isAborted())
            {
                return true;
            }

            // Lastly, tell anyone whose interested that the form is completely
            // submitted.

            resources.triggerContextEvent(EventConstants.SUBMIT, context, eventCallback);

            afterSuccessOrFailure();

            if (eventCallback.isAborted())
            {
                return true;
            }

            // For traditional request with no validation exceptions, re-render the
            // current page immediately, as-is.  Prior to Tapestry 5.4, a redirect was
            // sent that required that the tracker be persisted across requests.
            // See https://issues.apache.org/jira/browse/TAP5-1808

            if (tracker.getHasErrors() && !request.isXHR())
            {
                return STREAM_ACTIVE_PAGE_CONTENT;
            }

            // The event will not work its way up.

            return false;

        } finally
        {
            environment.pop(Heartbeat.class);
            environment.pop(FormSupport.class);

            environment.pop(ValidationTracker.class);

            if (didPushBeanValidationContext)
            {
                environment.pop(BeanValidationContext.class);
            }
        }
    }

    /**
     * A hook invoked from {@link #onAction(org.apache.tapestry5.EventContext)} after the
     * {@link org.apache.tapestry5.EventConstants#SUBMIT} or {@link org.apache.tapestry5.EventConstants#FAILURE} event has been triggered.
     *
     * This method will be invoked regardless of whether the submit or failure event was aborted.
     *
     * This implementation does nothing.
     *
     * @since 5.4
     */

    protected void afterSuccessOrFailure()
    {

    }

    /**
     * A hook invoked from {@link #onAction(org.apache.tapestry5.EventContext)} before any other setup.
     *
     * This implementation does nothing.
     *
     * @param context
     *         as passed to {@code onAction()}
     * @since 5.4
     */
    protected void beforeProcessSubmit(EventContext context)
    {

    }

    /**
     * A hook invoked from {@link #onAction(org.apache.tapestry5.EventContext)} after the
     * {@link org.apache.tapestry5.EventConstants#VALIDATE} event has been triggered, and
     * before the {@link #tracker} has been {@linkplain org.apache.tapestry5.ValidationTracker#clear() cleared}.
     *
     * Only invoked if the valiate event did not abort (that is, the no event handler method returned a value).
     *
     * This implementation does nothing.
     *
     * @since 5.4
     */
    protected void afterValidate()
    {

    }

    private boolean isFormCancelled()
    {
        // The "cancel" query parameter is reserved for this purpose; if it is present then the form was canceled on the
        // client side.  For image submits, there will be two parameters: "cancel.x" and "cancel.y".

        if (request.getParameter(InternalConstants.CANCEL_NAME) != null ||
                request.getParameter(InternalConstants.CANCEL_NAME + ".x") != null)
        {
            return true;
        }

        // When JavaScript is involved, it's more complicated. In fact, this is part of HLS's desire
        // to have all forms submit via XHR when JavaScript is present, since it would provide
        // an opportunity to get the submitting element's value into the request properly.

        String raw = request.getParameter(SUBMITTING_ELEMENT_ID);

        if (InternalUtils.isNonBlank(raw) &&
                new JSONArray(raw).getString(1).equals(InternalConstants.CANCEL_NAME))
        {
            return true;
        }

        return false;
    }


    private void fireValidateEvent(String eventName, EventContext context, TrackableComponentEventCallback callback)
    {
        try
        {
            resources.triggerContextEvent(eventName, context, callback);
        } catch (RuntimeException ex)
        {
            ValidationException ve = ExceptionUtils.findCause(ex, ValidationException.class, propertyAccess);

            if (ve != null)
            {
                ValidationTracker tracker = environment.peek(ValidationTracker.class);

                tracker.recordError(ve.getMessage());

                return;
            }

            throw ex;
        }
    }

    /**
     * Pulls the stored actions out of the request, converts them from MIME
     * stream back to object stream and then
     * objects, and executes them.
     */
    private void executeStoredActions(boolean forFormCancel)
    {
        String[] values = request.getParameters(FORM_DATA);

        if (!request.getMethod().equals("POST") || values == null)
            throw new RuntimeException(messages.format("core-invalid-form-request", FORM_DATA));

        // Due to Ajax there may be multiple values here, so
        // handle each one individually.

        for (String clientEncodedActions : values)
        {
            if (InternalUtils.isBlank(clientEncodedActions))
                continue;

            logger.debug("Processing actions: {}", clientEncodedActions);

            ObjectInputStream ois = null;

            Component component = null;

            try
            {
                ois = clientDataEncoder.decodeClientData(clientEncodedActions);

                while (!eventCallback.isAborted())
                {
                    String componentId = ois.readUTF();
                    boolean cancelAction = ois.readBoolean();
                    ComponentAction action = (ComponentAction) ois.readObject();

                    // Actions are a mix of ordinary actions and cancel actions.  Filter out one set or the other
                    // based on whether the form was submitted or cancelled.
                    if (forFormCancel != cancelAction)
                    {
                        continue;
                    }

                    component = source.getComponent(componentId);

                    logger.debug("Processing: {} {}", componentId, action);

                    action.execute(component);

                    component = null;
                }
            } catch (EOFException ex)
            {
                // Expected
            } catch (Exception ex)
            {
                Location location = component == null ? null : component.getComponentResources().getLocation();

                throw new TapestryException(ex.getMessage(), location, ex);
            } finally
            {
                InternalUtils.close(ois);
            }
        }
    }

    public void recordError(String errorMessage)
    {
        tracker.recordError(errorMessage);
    }

    public void recordError(Field field, String errorMessage)
    {
        tracker.recordError(field, errorMessage);
    }

    public boolean getHasErrors()
    {
        return tracker.getHasErrors();
    }

    public boolean isValid()
    {
        return !tracker.getHasErrors();
    }

    public void clearErrors()
    {
        tracker.clear();
    }

    // For testing:

    void setTracker(ValidationTracker tracker)
    {
        this.tracker = tracker;
    }

    /**
     * Forms use the same value for their name and their id attribute.
     */
    public String getClientId()
    {
        return clientId;
    }

    private void preallocateNames(IdAllocator idAllocator)
    {
        for (String name : formControlNameManager.getReservedNames())
        {
            idAllocator.allocateId(name);
            // See https://issues.apache.org/jira/browse/TAP5-1632
            javascriptSupport.allocateClientId(name);

        }

        Component activePage = componentSource.getActivePage();

        // This is unlikely but may be possible if people override some of the standard
        // exception reporting logic.

        if (activePage == null)
            return;

        ComponentResources activePageResources = activePage.getComponentResources();

        try
        {

            activePageResources.triggerEvent(EventConstants.PREALLOCATE_FORM_CONTROL_NAMES, new Object[]
                    {idAllocator}, null);
        } catch (RuntimeException ex)
        {
            logger.error(
                    String.format("Unable to obtain form control names to preallocate: %s",
                            ExceptionUtils.toMessage(ex)), ex);
        }
    }
}
