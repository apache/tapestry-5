// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.internal.ComponentActionSink;
import org.apache.tapestry5.corelib.internal.FormSupportImpl;
import org.apache.tapestry5.corelib.internal.InternalFormSupport;
import org.apache.tapestry5.corelib.mixins.RenderInformals;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.services.ComponentResultProcessorWrapper;
import org.apache.tapestry5.internal.services.HeartbeatImpl;
import org.apache.tapestry5.internal.util.AutofocusValidationDecorator;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.util.ExceptionUtils;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * An HTML form, which will enclose other components to render out the various types of fields.
 * <p/>
 * A Form emits many notification events. When it renders, it fires a {@link org.apache.tapestry5.EventConstants#PREPARE_FOR_RENDER}
 * notification, followed by a {@link org.apache.tapestry5.EventConstants#PREPARE} notification.
 * <p/>
 * When the form is submitted, the component emits several notifications: first a {@link
 * org.apache.tapestry5.EventConstants#PREPARE_FOR_SUBMIT}, then a {@link org.apache.tapestry5.EventConstants#PREPARE}:
 * these allow the page to update its state as necessary to prepare for the form submission, then (after components
 * enclosed by the form have operated), a {@link org.apache.tapestry5.EventConstants#VALIDATE_FORM} event is emitted, to
 * allow for cross-form validation. After that, either a {@link org.apache.tapestry5.EventConstants#SUCCESS} OR {@link
 * org.apache.tapestry5.EventConstants#FAILURE} event (depending on whether the {@link ValidationTracker} has recorded
 * any errors). Lastly, a {@link org.apache.tapestry5.EventConstants#SUBMIT} event, for any listeners that care only
 * about form submission, regardless of success or failure.
 * <p/>
 * For all of these notifications, the event context is derived from the <strong>context</strong> parameter. This
 * context is encoded into the form's action URI (the parameter is not read when the form is submitted, instead the
 * values encoded into the form are used).
 */
@Events({ EventConstants.PREPARE_FOR_RENDER, EventConstants.PREPARE, EventConstants.PREPARE_FOR_SUBMIT,
        EventConstants.VALIDATE_FORM,
        EventConstants.SUBMIT, EventConstants.FAILURE, EventConstants.SUCCESS })
public class Form implements ClientElement, FormValidationControl
{
    /**
     * @deprecated Use constant from {@link org.apache.tapestry5.EventConstants} instead.
     */
    public static final String PREPARE_FOR_RENDER = EventConstants.PREPARE_FOR_RENDER;

    /**
     * @deprecated Use constant from {@link org.apache.tapestry5.EventConstants} instead.
     */
    public static final String PREPARE_FOR_SUBMIT = EventConstants.PREPARE_FOR_SUBMIT;

    /**
     * @deprecated Use constant from {@link org.apache.tapestry5.EventConstants} instead.
     */
    public static final String PREPARE = EventConstants.PREPARE;

    /**
     * @deprecated Use constant from {@link org.apache.tapestry5.EventConstants} instead.
     */
    public static final String SUBMIT = EventConstants.SUBMIT;

    /**
     * @deprecated Use constant from {@link org.apache.tapestry5.EventConstants} instead.
     */
    public static final String VALIDATE_FORM = EventConstants.VALIDATE_FORM;

    /**
     * @deprecated Use constant from {@link org.apache.tapestry5.EventConstants} instead.
     */
    public static final String SUCCESS = EventConstants.SUCCESS;

    /**
     * @deprecated Use constant from {@link org.apache.tapestry5.EventConstants} instead.
     */
    public static final String FAILURE = EventConstants.FAILURE;

    /**
     * Query parameter name storing form data (the serialized commands needed to process a form submission).
     */
    public static final String FORM_DATA = "t:formdata";

    /**
     * The context for the link (optional parameter). This list of values will be converted into strings and included in
     * the URI. The strings will be coerced back to whatever their values are and made available to event handler
     * methods.
     */
    @Parameter
    private Object[] context;

    /**
     * The object which will record user input and validation errors. The object must be persistent between requests
     * (since the form submission and validation occurs in an component event request and the subsequent render occurs
     * in a render request). The default is a persistent property of the Form component and this is sufficient for
     * nearly all purposes (except when a Form is rendered inside a loop).
     */
    @Parameter("defaultTracker")
    private ValidationTracker tracker;

    @Inject
    @Symbol(SymbolConstants.FORM_CLIENT_LOGIC_ENABLED)
    private boolean clientLogicDefaultEnabled;

    /**
     * If true (the default) then client validation is enabled for the form, and the default set of JavaScript libraries
     * (Prototype, Scriptaculous and the Tapestry library) will be added to the rendered page, and the form will
     * register itself for validation. This may be turned off when client validation is not desired; for example, when
     * many validations are used that do not operate on the client side at all.
     */
    @Parameter
    private boolean clientValidation = clientLogicDefaultEnabled;

    /**
     * If true (the default), then the JavaScript will be added to position the cursor into the form. The field to
     * receive focus is the first rendered field that is in error, or required, or present (in that order of priority).
     *
     * @see SymbolConstants#FORM_CLIENT_LOGIC_ENABLED
     */
    @Parameter
    private boolean autofocus = clientLogicDefaultEnabled;

    /**
     * Binding the zone parameter will cause the form submission to be handled as an Ajax request that updates the
     * indicated zone.  Often a Form will update the same zone that contains it.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String zone;

    /**
     * Prefix value used when searching for validation messages and constraints.  The default is the Form component's
     * id. This is overriden by {@link org.apache.tapestry5.corelib.components.BeanEditForm}.
     *
     * @see org.apache.tapestry5.services.FormSupport#getFormValidationId()
     */
    @Parameter
    private String validationId;

    @Inject
    private Logger logger;

    @Inject
    private Environment environment;

    @Inject
    private ComponentResources resources;

    @Inject
    private Messages messages;

    @Environmental
    private RenderSupport renderSupport;

    @Inject
    private Request request;

    @Inject
    private ComponentSource source;

    @Persist(PersistenceConstants.FLASH)
    private ValidationTracker defaultTracker;

    private InternalFormSupport formSupport;

    private Element form;

    private Element div;

    // Collects a stream of component actions. Each action goes in as a UTF string (the component
    // component id), followed by a ComponentAction

    private ComponentActionSink actionSink;

    @Mixin
    private RenderInformals renderInformals;

    /**
     * Set up via the traditional or Ajax component event request handler
     */
    @Environmental
    private ComponentEventResultProcessor componentEventResultProcessor;

    @Environmental
    private ClientBehaviorSupport clientBehaviorSupport;

    @Inject
    private ClientDataEncoder clientDataEncoder;

    private String name;

    String defaultValidationId()
    {
        return resources.getId();
    }

    public ValidationTracker getDefaultTracker()
    {
        if (defaultTracker == null) defaultTracker = new ValidationTrackerImpl();

        return defaultTracker;
    }

    public void setDefaultTracker(ValidationTracker defaultTracker)
    {
        this.defaultTracker = defaultTracker;
    }

    void setupRender()
    {
        FormSupport existing = environment.peek(FormSupport.class);

        if (existing != null)
            throw new TapestryException(messages.get("nesting-not-allowed"), existing, null);
    }

    void beginRender(MarkupWriter writer)
    {
        Link link = resources.createFormEventLink(EventConstants.ACTION, context);

        actionSink = new ComponentActionSink(logger, clientDataEncoder);

        name = renderSupport.allocateClientId(resources);

        formSupport = createRenderTimeFormSupport(name, actionSink, new IdAllocator());

        if (zone != null) clientBehaviorSupport.linkZone(name, zone, link);

        // TODO: Forms should not allow to nest. Perhaps a set() method instead of a push() method
        // for this kind of check?  

        environment.push(FormSupport.class, formSupport);
        environment.push(ValidationTracker.class, tracker);

        if (autofocus)
        {
            ValidationDecorator autofocusDecorator = new AutofocusValidationDecorator(environment.peek(
                    ValidationDecorator.class), tracker, renderSupport);
            environment.push(ValidationDecorator.class, autofocusDecorator);
        }

        // Now that the environment is setup, inform the component or other listeners that the form
        // is about to render.  

        resources.triggerEvent(EventConstants.PREPARE_FOR_RENDER, context, null);

        resources.triggerEvent(EventConstants.PREPARE, context, null);

        // Save the form element for later, in case we want to write an encoding type attribute.

        form = writer.element("form",
                              "name", name,
                              "id", name,
                              "method", "post",
                              "action", link);

        if ((zone != null || clientValidation) && !request.isXHR())
            writer.attributes("onsubmit", MarkupConstants.WAIT_FOR_PAGE);

        resources.renderInformalParameters(writer);

        div = writer.element("div", "class", CSSClassConstants.INVISIBLE);

        for (String parameterName : link.getParameterNames())
        {
            String value = link.getParameterValue(parameterName);

            writer.element("input",
                           "type", "hidden",
                           "name", parameterName,
                           "value", value);
            writer.end();
        }

        writer.end(); // div

        environment.peek(Heartbeat.class).begin();
    }

    /**
     * Creates an {@link org.apache.tapestry5.corelib.internal.InternalFormSupport} for this Form. This method is used
     * by {@link org.apache.tapestry5.corelib.components.FormInjector}.
     *
     * @param name       the client-side name and client id for the rendered form element
     * @param actionSink used to collect component actions that will, ultimately, be written as the t:formdata hidden
     *                   field
     * @param allocator  used to allocate unique ids
     * @return form support object
     */
    InternalFormSupport createRenderTimeFormSupport(String name, ComponentActionSink actionSink, IdAllocator allocator)
    {
        return new FormSupportImpl(resources, name, actionSink, clientBehaviorSupport,
                                   clientValidation, allocator, validationId);
    }

    void afterRender(MarkupWriter writer)
    {
        environment.peek(Heartbeat.class).end();

        formSupport.executeDeferred();

        String encodingType = formSupport.getEncodingType();

        if (encodingType != null) form.forceAttributes("enctype", encodingType);

        writer.end(); // form

        div.element("input",
                    "type", "hidden",
                    "name", FORM_DATA,
                    "value", actionSink.getClientData());

        if (autofocus)
            environment.pop(ValidationDecorator.class);
    }

    void cleanupRender()
    {
        environment.pop(FormSupport.class);

        formSupport = null;

        environment.pop(ValidationTracker.class);
    }

    @SuppressWarnings({ "unchecked", "InfiniteLoopStatement" })
    @Log
    Object onAction(EventContext context) throws IOException
    {
        tracker.clear();

        formSupport = new FormSupportImpl(resources, validationId);

        environment.push(ValidationTracker.class, tracker);
        environment.push(FormSupport.class, formSupport);

        Heartbeat heartbeat = new HeartbeatImpl();

        environment.push(Heartbeat.class, heartbeat);

        heartbeat.begin();

        try
        {
            ComponentResultProcessorWrapper callback = new ComponentResultProcessorWrapper(
                    componentEventResultProcessor);

            resources.triggerContextEvent(EventConstants.PREPARE_FOR_SUBMIT, context, callback);

            if (callback.isAborted()) return true;

            resources.triggerContextEvent(EventConstants.PREPARE, context, callback);

            if (callback.isAborted()) return true;

            executeStoredActions();

            heartbeat.end();

            formSupport.executeDeferred();

            fireValidateFormEvent(context, callback);

            if (callback.isAborted()) return true;

            // Let the listeners know about overall success or failure. Most listeners fall into
            // one of those two camps.

            // If the tracker has no errors, then clear it of any input values
            // as well, so that the next page render will be "clean" and show
            // true persistent data, not value from the previous form submission.

            if (!tracker.getHasErrors())
                tracker.clear();

            resources.triggerContextEvent(tracker.getHasErrors() ? EventConstants.FAILURE : EventConstants.SUCCESS,
                                          context, callback);

            // Lastly, tell anyone whose interested that the form is completely submitted.

            if (callback.isAborted()) return true;

            resources.triggerContextEvent(EventConstants.SUBMIT, context, callback);

            return callback.isAborted();
        }
        finally
        {
            environment.pop(Heartbeat.class);
            environment.pop(FormSupport.class);

            // This forces an update that feeds through the system and gets the updated
            // state of the tracker (if using the Form's defaultTracker property, which is flash persisted)
            // stored back into the session.

            tracker = environment.pop(ValidationTracker.class);
        }
    }

    private void fireValidateFormEvent(EventContext context, ComponentResultProcessorWrapper callback)
    {
        try
        {
            resources.triggerContextEvent(EventConstants.VALIDATE_FORM, context, callback);
        }
        catch (RuntimeException ex)
        {
            ValidationException ve = ExceptionUtils.findCause(ex, ValidationException.class);

            if (ve != null)
            {
                recordError(ve.getMessage());
                return;
            }

            throw ex;
        }
    }

    /**
     * Pulls the stored actions out of the request, converts them from MIME stream back to object stream and then
     * objects, and executes them.
     */
    private void executeStoredActions()
    {
        String[] values = request.getParameters(FORM_DATA);

        if (!request.getMethod().equals("POST") || values == null)
            throw new RuntimeException(messages.format("invalid-request", FORM_DATA));

        // Due to Ajax (FormInjector) there may be multiple values here, so handle each one individually.

        for (String clientEncodedActions : values)
        {
            if (InternalUtils.isBlank(clientEncodedActions)) continue;

            logger.debug("Processing actions: {}", clientEncodedActions);

            ObjectInputStream ois = null;

            Component component = null;

            try
            {
                ois = clientDataEncoder.decodeClientData(clientEncodedActions);

                while (true)
                {
                    String componentId = ois.readUTF();
                    ComponentAction action = (ComponentAction) ois.readObject();

                    component = source.getComponent(componentId);

                    logger.debug("Processing: {} {}", componentId, action);

                    action.execute(component);

                    component = null;
                }
            }
            catch (EOFException ex)
            {
                // Expected
            }
            catch (Exception ex)
            {
                Location location = component == null ? null : component.getComponentResources().getLocation();

                throw new TapestryException(ex.getMessage(), location, ex);
            }
            finally
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

    // For testing:

    void setTracker(ValidationTracker tracker)
    {
        this.tracker = tracker;
    }

    public void clearErrors()
    {
        tracker.clear();
    }

    /**
     * Forms use the same value for their name and their id attribute.
     */
    public String getClientId()
    {
        return name;
    }
}
