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

package org.apache.tapestry5.services;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.runtime.Event;

import java.lang.reflect.Modifier;

/**
 * Constants used by implementations of {@link org.apache.tapestry5.services.ComponentClassTransformWorker}.
 * <p/>
 * Note: methods on transformed components will not be invoked <em>unless</em> {@linkplain
 * org.apache.tapestry5.model.MutableComponentModel#addRenderPhase(Class) the component model is updated to identify the
 * use of the corresponding render phase}.
 */
public final class TransformConstants
{
    // Shared parameters of a whole bunch of lifecycle methods, representing the different
    // component render states.
    private static final String[] RENDER_PHASE_METHOD_PARAMETERS = { MarkupWriter.class.getName(),
            Event.class.getName() };

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#dispatchComponentEvent(org.apache.tapestry5.runtime.ComponentEvent)}.
     *
     * @see org.apache.tapestry5.annotations.OnEvent
     */
    public static final TransformMethodSignature DISPATCH_COMPONENT_EVENT = new TransformMethodSignature(
            Modifier.PUBLIC, "boolean", "dispatchComponentEvent", new String[] { ComponentEvent.class.getName() },
            null);

    /**
     * Signature for {@link org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidLoad()}.
     */
    public static final TransformMethodSignature CONTAINING_PAGE_DID_LOAD_SIGNATURE = new TransformMethodSignature(
            "containingPageDidLoad");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#postRenderCleanup()}.
     */
    public static final TransformMethodSignature POST_RENDER_CLEANUP_SIGNATURE = new TransformMethodSignature(
            "postRenderCleanup");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidDetach()}.
     */
    public static final TransformMethodSignature CONTAINING_PAGE_DID_DETACH_SIGNATURE = new TransformMethodSignature(
            "containingPageDidDetach");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidAttach()}.
     */
    public static final TransformMethodSignature CONTAINING_PAGE_DID_ATTACH_SIGNATURE = new TransformMethodSignature(
            "containingPageDidAttach");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.PageLifecycleListener#restoreStateBeforePageAttach()}
     *
     * @since 5.1.0.1
     */
    public static final TransformMethodSignature RESTORE_STATE_BEFORE_PAGE_ATTACH_SIGNATURE = new TransformMethodSignature(
            "restoreStateBeforePageAttach");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#setupRender(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.SetupRender
     */
    public static final TransformMethodSignature SETUP_RENDER_SIGNATURE = renderPhaseSignature("setupRender");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#beginRender(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeginRender
     */
    public static final TransformMethodSignature BEGIN_RENDER_SIGNATURE = renderPhaseSignature("beginRender");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#beforeRenderTemplate(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeforeRenderTemplate
     */
    public static final TransformMethodSignature BEFORE_RENDER_TEMPLATE_SIGNATURE = renderPhaseSignature(
            "beforeRenderTemplate");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#afterRenderTemplate(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeforeRenderTemplate
     */
    public static final TransformMethodSignature AFTER_RENDER_TEMPLATE_SIGNATURE = renderPhaseSignature(
            "afterRenderTemplate");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#beforeRenderBody(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeforeRenderBody
     */
    public static final TransformMethodSignature BEFORE_RENDER_BODY_SIGNATURE = renderPhaseSignature(
            "beforeRenderBody");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#afterRenderBody(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.AfterRenderBody
     */
    public static final TransformMethodSignature AFTER_RENDER_BODY_SIGNATURE = renderPhaseSignature("afterRenderBody");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#afterRender(MarkupWriter, Event)}
     *
     * @see org.apache.tapestry5.annotations.AfterRender
     */
    public static final TransformMethodSignature AFTER_RENDER_SIGNATURE = renderPhaseSignature("afterRender");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#cleanupRender(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.CleanupRender
     */
    public static final TransformMethodSignature CLEANUP_RENDER_SIGNATURE = renderPhaseSignature("cleanupRender");

    private static TransformMethodSignature renderPhaseSignature(String name)
    {
        return new TransformMethodSignature(Modifier.PUBLIC, "void", name, RENDER_PHASE_METHOD_PARAMETERS, null);
    }
}
