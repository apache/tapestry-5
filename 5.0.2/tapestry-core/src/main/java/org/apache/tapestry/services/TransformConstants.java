// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.services;

import java.lang.reflect.Modifier;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.runtime.ComponentEvent;
import org.apache.tapestry.runtime.Event;

/**
 * Constants used by implementations of
 * {@link org.apache.tapestry.services.ComponentClassTransformWorker}.
 */
public final class TransformConstants
{
    // Shared parameters of a whole bunch of lifecycle methods, representing the different
    // component render states.
    private static final String[] RENDER_PHASE_METHOD_PARAMETERS =
    { MarkupWriter.class.getName(), Event.class.getName() };

    /**
     * Signature for {@link org.apache.tapestry.runtime.Component#handleEvent(Event event)
     * 
     * @see org.apache.tapestry.annotations.OnEvent
     */
    public static final MethodSignature HANDLE_COMPONENT_EVENT = new MethodSignature(
            Modifier.PUBLIC, "boolean", "handleComponentEvent", new String[]
            { ComponentEvent.class.getName() }, null);

    /**
     * Signature for
     * {@link org.apache.tapestry.runtime.PageLifecycleListener#containingPageDidLoad()}.
     */
    public static final MethodSignature CONTAINING_PAGE_DID_LOAD_SIGNATURE = new MethodSignature(
            "containingPageDidLoad");

    /** Signature for {@link org.apache.tapestry.runtime.Component#postRenderCleanup()}. */
    public static final MethodSignature POST_RENDER_CLEANUP_SIGNATURE = new MethodSignature(
            "postRenderCleanup");

    /**
     * Signature for
     * {@link org.apache.tapestry.runtime.PageLifecycleListener#containingPageDidDetach()}.
     */
    public static final MethodSignature CONTAINING_PAGE_DID_DETACH_SIGNATURE = new MethodSignature(
            "containingPageDidDetach");

    /**
     * Signature for
     * {@link org.apache.tapestry.runtime.PageLifecycleListener#containingPageDidAttach()}.
     */
    public static final MethodSignature CONTAINING_PAGE_DID_ATTACH_SIGNATURE = new MethodSignature(
            "containingPageDidAttach");

    /**
     * Signature for {@link org.apache.tapestry.runtime.Component#setupRender(MarkupWriter, Event)}.
     * 
     * @see org.apache.tapestry.annotations.SetupRender
     */
    public static final MethodSignature SETUP_RENDER_SIGNATURE = renderPhaseSignature("setupRender");

    /**
     * Signature for {@link org.apache.tapestry.runtime.Component#beginRender(MarkupWriter, Event)}.
     * 
     * @see org.apache.tapestry.annotations.BeginRender
     */
    public static final MethodSignature BEGIN_RENDER_SIGNATURE = renderPhaseSignature("beginRender");

    /**
     * Signature for
     * {@link org.apache.tapestry.runtime.Component#beforeRenderTemplate(MarkupWriter, Event)}.
     * 
     * @see org.apache.tapestry.annotations.BeforeRenderTemplate
     */
    public static MethodSignature BEFORE_RENDER_TEMPLATE_SIGNATURE = renderPhaseSignature("beforeRenderTemplate");

    /**
     * Signature for
     * {@link org.apache.tapestry.runtime.Component#afterRenderTemplate(MarkupWriter, Event)}.
     * 
     * @see org.apache.tapestry.annotations.BeforeRenderTemplate
     */
    public static MethodSignature AFTER_RENDER_TEMPLATE_SIGNATURE = renderPhaseSignature("afterRenderTemplate");

    /**
     * Signature for
     * {@link org.apache.tapestry.runtime.Component#beforeRenderBody(MarkupWriter, Event)}.
     * 
     * @see org.apache.tapestry.annotations.BeforeRenderBody
     */
    public static final MethodSignature BEFORE_RENDER_BODY_SIGNATURE = renderPhaseSignature("beforeRenderBody");

    /**
     * Signature for
     * {@link org.apache.tapestry.runtime.Component#afterRenderBody(MarkupWriter, Event)}.
     * 
     * @see org.apache.tapestry.annotations.AfterRenderBody
     */
    public static final MethodSignature AFTER_RENDER_BODY_SIGNATURE = renderPhaseSignature("afterRenderBody");

    /**
     * Signature for {@link org.apache.tapestry.runtime.Component#afterRender(MarkupWriter, Event)}
     * 
     * @see org.apache.tapestry.annotations.AfterRender
     */
    public static final MethodSignature AFTER_RENDER_SIGNATURE = renderPhaseSignature("afterRender");

    /**
     * Signature for
     * {@link org.apache.tapestry.runtime.Component#cleanupRender(MarkupWriter, Event)}.
     * 
     * @see org.apache.tapestry.annotations.CleanupRender
     */
    public static final MethodSignature CLEANUP_RENDER_SIGNATURE = renderPhaseSignature("cleanupRender");

    private TransformConstants()
    {
    }

    private static MethodSignature renderPhaseSignature(String name)
    {
        return new MethodSignature(Modifier.PUBLIC, "void", name, RENDER_PHASE_METHOD_PARAMETERS,
                null);
    }
}
