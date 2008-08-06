// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.corelib.mixins.RenderInformals;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.services.ClientBehaviorSupport;
import org.apache.tapestry5.internal.services.ComponentInvocationMap;
import org.apache.tapestry5.internal.services.ComponentResultProcessorWrapper;
import org.apache.tapestry5.internal.services.HeartbeatImpl;
import org.apache.tapestry5.internal.util.Base64ObjectInputStream;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * An HTML form, which will enclose other components to render out the various types of fields.
 * <p/>
 * A Form emits many notification events. When it renders, it fires a {@link #PREPARE_FOR_RENDER} notification, followed
 * by a {@link #PREPARE} notification.
 * <p/>
 * When the form is submitted, the component emits several notifications: first a {@link #PREPARE_FOR_SUBMIT}, then a
 * {@link #PREPARE}: these allow the page to update its state as necessary to prepare for the form submission, then
 * (after components enclosed by the form have operated), a {@link #VALIDATE_FORM}event is emitted, to allow for
 * cross-form validation. After that, either a {@link #SUCCESS} OR {@link #FAILURE} event (depending on whether the
 * {@link ValidationTracker} has recorded any errors). Lastly, a {@link #SUBMIT} event, for any listeners that care only
 * about form submission, regardless of success or failure.
 * <p/>
 * For all of these notifications, the event context is derived from the <strong>context</strong> parameter. This
 * context is encoded into the form's action URI (the parameter is not read when the form is submitted, instead the
 * values encoded into the form are used).
 */
public class Form implements ClientElement, FormValidationControl
{
    /**
     * Invoked before {@link #PREPARE} when rendering out the form.
     */
    public static final String PREPARE_FOR_RENDER = "prepareForRender";

    /**
     * Invoked before {@link #PREPARE} when the form is submitted.
     */
    public static final String PREPARE_FOR_SUBMIT = "prepareForSubmit";

    /**
     * Invoked to let the containing component(s) prepare for the form rendering or the form submission.
     */
    public static final String PREPARE = "prepare";

    /**
     * Event type for a notification after the form has submitted. This event notification occurs on any form submit,
     * without respect to "success" or "failure".
     */
    public static final String SUBMIT = "submit";

    /**
     * Event type for a notification to perform validation of submitted data. This allows a listener to perform
     * cross-field validation. This occurs before the {@link #SUCCESS} or {@link #FAILURE} notification.
     */
    public static final String VALIDATE_FORM = "validateForm";

    /**
     * Event type for a notification after the form has submitted, when there are no errors in the validation tracker.
     * This occurs before the {@link #SUBMIT} event.
     */
    public static final String SUCCESS = "success";

    /**
     * Event type for a notification after the form has been submitted, when there are errors in the validation tracker.
     * This occurs before the {@link #SUBMIT} event.
     */
    public static final String FAILURE = "failure";

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
    private List<?> context;

    /**
     * The object which will record user input and validation errors. The object must be persistent between requests
     * (since the form submission and validation occurs in an component event request and the subsequent render occurs
     * in a render request). The default is a persistent property of the Form component and this is sufficient for
     * nearly all purposes (except when a Form is rendered inside a loop).
     */
    @Parameter("defaultTracker")
    private ValidationTracker tracker;

    /**
     * If true (the default) then client validation is enabled for the form, and the default set of JavaScript libraries
     * (Prototype, Scriptaculous and the Tapestry library) will be added to the rendered page, and the form will
     * register itself for validation. This may be turned off when client validation is not desired; for example, when
     * many validations are used that do not operate on the client side at all.
     */
    @Parameter("true")
    private boolean clientValidation;

    /**
     * Binding the zone parameter will cause the form submission to be handled as an Ajax request that updates the
     * indicated zone.  Often a Form will update the same zone that contains it.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String zone;

    @Inject
    private Logger logger;

    @Inject
    private Environment environment;

    @Inject
    private ComponentResources resources;

    @Environmental
    private RenderSupport renderSupport;

    @Inject
    private Request request;

    @Inject
    private ComponentSource source;

    @Persist(PersistenceConstants.FLASH)
    private ValidationTracker defaultTracker;

    @Inject
    private ComponentInvocationMap componentInvocationMap;

    private FormSupportImpl formSupport;

    private Element form;

    private Element div;

    // Collects a stream of component actions. Each action goes in as a UTF string (the component
    // component id), followed by a ComponentAction

    private ComponentActionSink actionSink;

    @SuppressWarnings("unused")
    @Mixin
    private RenderInformals renderInformals;

    /**
     * Set up via the traditional or Ajax component event request handler
     */
    @Environmental
    private ComponentEventResultProcessor componentEventResultProcessor;

    @Environmental
    private ClientBehaviorSupport clientBehaviorSupport;

    private String name;

    public ValidationTracker getDefaultTracker()
    {
        if (defaultTracker == null) defaultTracker = new ValidationTrackerImpl();

        return defaultTracker;
    }

    public void setDefaultTracker(ValidationTracker defaultTracker)
    {
        this.defaultTracker = defaultTracker;
    }

    void beginRender(MarkupWriter writer)
    {

        actionSink = new ComponentActionSink(logger);

        name = renderSupport.allocateClientId(resources);

        formSupport = new FormSupportImpl(name, actionSink, clientBehaviorSupport, clientValidation);

        if (zone != null) clientBehaviorSupport.linkZone(name, zone);

        // TODO: Forms should not allow to nest. Perhaps a set() method instead of a push() method
        // for this kind of check?  

        environment.push(FormSupport.class, formSupport);
        environment.push(ValidationTracker.class, tracker);

        // Now that the environment is setup, inform the component or other listeners that the form
        // is about to render.  

        Object[] contextArray = context == null ? new Object[0] : context.toArray();

        resources.triggerEvent(PREPARE_FOR_RENDER, contextArray, null);

        resources.triggerEvent(PREPARE, contextArray, null);

        Link link = resources.createActionLink(EventConstants.ACTION, true, contextArray);

        // Save the form element for later, in case we want to write an encoding type attribute.

        form = writer.element("form",
                              "name", name,
                              "id", name,
                              "method", "post",
                              "action", link);

        componentInvocationMap.store(form, link);

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
                    "value", actionSink.toBase64());
    }

    void cleanupRender()
    {
        environment.pop(FormSupport.class);

        formSupport = null;

        // This forces a change to the tracker, which is nice because its internal state has
        // changed.
        tracker = environment.pop(ValidationTracker.class);
    }

    @SuppressWarnings({"unchecked", "InfiniteLoopStatement"})
    @Log
    Object onAction(EventContext context) throws IOException
    {
        tracker.clear();

        formSupport = new FormSupportImpl();

        environment.push(ValidationTracker.class, tracker);
        environment.push(FormSupport.class, formSupport);

        Heartbeat heartbeat = new HeartbeatImpl();

        environment.push(Heartbeat.class, heartbeat);

        heartbeat.begin();

        try
        {
            ComponentResultProcessorWrapper callback = new ComponentResultProcessorWrapper(
                    componentEventResultProcessor);

            resources.triggerContextEvent(PREPARE_FOR_SUBMIT, context, callback);

            if (callback.isAborted()) return true;

            resources.triggerContextEvent(PREPARE, context, callback);

            if (callback.isAborted()) return true;

            executeStoredActions();

            heartbeat.end();

            ValidationTracker tracker = environment.peek(ValidationTracker.class);

            // Let the listeners peform any final validations

            // Update through the parameter because the tracker has almost certainly changed
            // internal state.

            this.tracker = tracker;

            formSupport.executeDeferred();

            resources.triggerContextEvent(VALIDATE_FORM, context, callback);

            if (callback.isAborted()) return true;

            // Let the listeners know about overall success or failure. Most listeners fall into
            // one of those two camps.

            // If the tracker has no errors, then clear it of any input values
            // as well, so that the next page render will be "clean" and show
            // true persistent data, not value from the previous form submission.

            if (!this.tracker.getHasErrors()) this.tracker.clear();

            resources.triggerContextEvent(tracker.getHasErrors() ? FAILURE : SUCCESS, context, callback);

            // Lastly, tell anyone whose interested that the form is completely submitted.

            if (callback.isAborted()) return true;

            resources.triggerContextEvent(SUBMIT, context, callback);

            return callback.isAborted();
        }
        finally
        {
            environment.pop(Heartbeat.class);
            environment.pop(FormSupport.class);
        }
    }

    /**
     * Pulls the stored actions out of the request, converts them from MIME stream back to object stream and then
     * objects, and executes them.
     */
    private void executeStoredActions()
    {
        String[] values = request.getParameters(FORM_DATA);

        if (values == null) return;

        // Due to Ajax (FormInjector) there may be multiple values here, so handle each one individually.

        for (String actionsBase64 : values)
        {
            if (logger.isDebugEnabled())
                logger.debug(String.format("Processing actions: %s", actionsBase64));

            ObjectInputStream ois = null;

            Component component = null;

            try
            {
                ois = new Base64ObjectInputStream(actionsBase64);

                while (true)
                {
                    String componentId = ois.readUTF();
                    ComponentAction action = (ComponentAction) ois.readObject();

                    component = source.getComponent(componentId);

                    if (logger.isDebugEnabled())
                        logger.debug(String.format("Processing: %s  %s", componentId, action));


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
        ValidationTracker tracker = this.tracker;

        tracker.recordError(errorMessage);

        this.tracker = tracker;
    }

    public void recordError(Field field, String errorMessage)
    {
        ValidationTracker tracker = this.tracker;

        tracker.recordError(field, errorMessage);

        this.tracker = tracker;
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
