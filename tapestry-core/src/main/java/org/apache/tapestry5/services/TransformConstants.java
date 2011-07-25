// Copyright 2006, 2007, 2008, 2009, 2011 The Apache Software Foundation
//
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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.structure.PageResetListener;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.runtime.Event;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;

import java.lang.reflect.Modifier;

/**
 * Constants used by implementations of {@link org.apache.tapestry5.services.ComponentClassTransformWorker} and
 * {@link ComponentClassTransformWorker2}.
 * <p/>
 * Note: methods on transformed components will not be invoked <em>unless</em>
 * {@linkplain org.apache.tapestry5.model.MutableComponentModel#addRenderPhase(Class) the component model is updated to
 * identify the use of the corresponding render phase}.
 */
public final class TransformConstants
{
    // Shared parameters of a whole bunch of lifecycle methods, representing the different
    // component render states.
    private static final String[] RENDER_PHASE_METHOD_PARAMETERS =
            {MarkupWriter.class.getName(), Event.class.getName()};

    /**
     * Signature for
     * {@link org.apache.tapestry5.runtime.Component#dispatchComponentEvent(org.apache.tapestry5.runtime.ComponentEvent)}
     * .
     *
     * @see org.apache.tapestry5.annotations.OnEvent
     * @deprecated Deprecated in Tapestry 5.3, use {@link #DISPATCH_COMPONENT_EVENT_DESCRIPTION}.
     */
    public static final TransformMethodSignature DISPATCH_COMPONENT_EVENT = new TransformMethodSignature(
            Modifier.PUBLIC, "boolean", "dispatchComponentEvent", new String[]
            {ComponentEvent.class.getName()}, null);

    /**
     * Description for
     * {@link org.apache.tapestry5.runtime.Component#dispatchComponentEvent(org.apache.tapestry5.runtime.ComponentEvent)}
     * .
     *
     * @see org.apache.tapestry5.annotations.OnEvent
     * @since 5.3
     */
    public static final MethodDescription DISPATCH_COMPONENT_EVENT_DESCRIPTION = PlasticUtils.getMethodDescription(
            Component.class, "dispatchComponentEvent", ComponentEvent.class);

    /**
     * Signature for {@link org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidLoad()}.
     *
     * @deprecated Deprecated in 5.3, use {@link #CONTAINING_PAGE_DID_LOAD_DESCRIPTION}.
     */
    public static final TransformMethodSignature CONTAINING_PAGE_DID_LOAD_SIGNATURE = new TransformMethodSignature(
            "containingPageDidLoad");

    /**
     * Description for {@link org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidLoad()}.
     *
     * @since 5.3
     */
    public static final MethodDescription CONTAINING_PAGE_DID_LOAD_DESCRIPTION = PlasticUtils.getMethodDescription(
            PageLifecycleListener.class, "containingPageDidLoad");

    /**
     * Description for {@link org.apache.tapestry5.internal.structure.PageResetListener#containingPageDidReset()}. Note that the {@link PageResetListener}
     * interface is not automatically implemented by components. ]
     *
     * @see org.apache.tapestry5.annotations.PageReset
     * @see org.apache.tapestry5.internal.transform.PageResetAnnotationWorker
     * @since 5.3
     */
    public static final MethodDescription CONTAINING_PAGE_DID_RESET_DESCRIPTION = PlasticUtils.getMethodDescription(PageResetListener.class, "containingPageDidReset");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#postRenderCleanup()}.
     */
    public static final TransformMethodSignature POST_RENDER_CLEANUP_SIGNATURE = new TransformMethodSignature(
            "postRenderCleanup");


    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#postRenderCleanup()}.
     *
     * @since 5.3
     */
    public static final MethodDescription POST_RENDER_CLEANUP_DESCRIPTION = PlasticUtils.getMethodDescription(Component.class, "postRenderCleanup");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidDetach()}.
     *
     * @deprecated Deprecated in Tapestry 5.3, use {@link #CONTAINING_PAGE_DID_DETACH_DESCRIPTION}
     */
    public static final TransformMethodSignature CONTAINING_PAGE_DID_DETACH_SIGNATURE = new TransformMethodSignature(
            "containingPageDidDetach");

    /**
     * Description for {@link org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidDetach()}.
     *
     * @since 5.3
     * @deprecated Deprecated in 5.3, with {@link org.apache.tapestry5.annotations.PageDetached}.
     */
    public static final MethodDescription CONTAINING_PAGE_DID_DETACH_DESCRIPTION = PlasticUtils.getMethodDescription(PageLifecycleListener.class, "containingPageDidDetach");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidAttach()}.
     *
     * @deprecated Deprecated in Tapestry 5.3, use {@link #CONTAINING_PAGE_DID_ATTACH_DESCRIPTION}
     */
    public static final TransformMethodSignature CONTAINING_PAGE_DID_ATTACH_SIGNATURE = new TransformMethodSignature(
            "containingPageDidAttach");

    /**
     * Description for {@link org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidAttach()}.
     *
     * @since 5.3
     * @deprecated Deprecated in 5.3, along with {@link org.apache.tapestry5.annotations.PageAttached}.
     */
    public static final MethodDescription CONTAINING_PAGE_DID_ATTACH_DESCRIPTION = PlasticUtils.getMethodDescription(PageLifecycleListener.class, "containingPageDidAttach");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.PageLifecycleListener#restoreStateBeforePageAttach()}
     *
     * @since 5.1.0.1
     * @deprecated Deprecated in 5.3, with no replacement.
     */
    public static final TransformMethodSignature RESTORE_STATE_BEFORE_PAGE_ATTACH_SIGNATURE = new TransformMethodSignature(
            "restoreStateBeforePageAttach");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#setupRender(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.SetupRender
     * @deprecated Deprecated in Tapestry 5.3, use {@link #SETUP_RENDER_DESCRIPTION}
     */
    public static final TransformMethodSignature SETUP_RENDER_SIGNATURE = renderPhaseSignature("setupRender");

    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#setupRender(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.SetupRender
     * @since 5.3
     */
    public static final MethodDescription SETUP_RENDER_DESCRIPTION = renderPhaseDescription("setupRender");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#beginRender(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeginRender
     * @deprecated Deprecated in Tapestry 5.3, use {@link #BEGIN_RENDER_DESCRIPTION}
     */
    public static final TransformMethodSignature BEGIN_RENDER_SIGNATURE = renderPhaseSignature("beginRender");

    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#beginRender(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeginRender
     * @since 5.3
     */
    public static final MethodDescription BEGIN_RENDER_DESCRIPTION = renderPhaseDescription("beginRender");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#beforeRenderTemplate(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeforeRenderTemplate
     * @deprecated Deprecated in Tapestry 5.3, use {@link #BEFORE_RENDER_TEMPLATE_DESCRIPTION}
     */
    public static final TransformMethodSignature BEFORE_RENDER_TEMPLATE_SIGNATURE = renderPhaseSignature("beforeRenderTemplate");

    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#beforeRenderTemplate(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeforeRenderTemplate
     * @since 5.3
     */
    public static final MethodDescription BEFORE_RENDER_TEMPLATE_DESCRIPTION = renderPhaseDescription("beforeRenderTemplate");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#afterRenderTemplate(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeforeRenderTemplate
     * @deprecated Deprecated in Tapestry 5.3, use {@link #AFTER_RENDER_TEMPLATE_DESCRIPTION}
     */
    public static final TransformMethodSignature AFTER_RENDER_TEMPLATE_SIGNATURE = renderPhaseSignature("afterRenderTemplate");

    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#afterRenderTemplate(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeforeRenderTemplate
     * @since 5.3
     */
    public static final MethodDescription AFTER_RENDER_TEMPLATE_DESCRIPTION = renderPhaseDescription("afterRenderTemplate");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#beforeRenderBody(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeforeRenderBody
     * @deprecated Deprecated in Tapestry 5.3, use {@link #BEFORE_RENDER_BODY_DESCRIPTION}
     */
    public static final TransformMethodSignature BEFORE_RENDER_BODY_SIGNATURE = renderPhaseSignature("beforeRenderBody");

    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#beforeRenderBody(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeforeRenderBody
     * @since 5.3
     */
    public static final MethodDescription BEFORE_RENDER_BODY_DESCRIPTION = renderPhaseDescription("beforeRenderBody");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#afterRenderBody(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.AfterRenderBody
     * @deprecated Deprecated in Tapestry 5.3, use {@link #AFTER_RENDER_BODY_DESCRIPTION}
     */
    public static final TransformMethodSignature AFTER_RENDER_BODY_SIGNATURE = renderPhaseSignature("afterRenderBody");


    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#afterRenderBody(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.AfterRenderBody
     * @since 5.3
     */
    public static final MethodDescription AFTER_RENDER_BODY_DESCRIPTION = renderPhaseDescription("afterRenderBody");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#afterRender(MarkupWriter, Event)}
     *
     * @see org.apache.tapestry5.annotations.AfterRender
     * @deprecated Deprecated in Tapestry 5.3, use {@link #AFTER_RENDER_DESCRIPTION}
     */
    public static final TransformMethodSignature AFTER_RENDER_SIGNATURE = renderPhaseSignature("afterRender");

    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#afterRender(MarkupWriter, Event)}
     *
     * @see org.apache.tapestry5.annotations.AfterRender
     * @since 5.3
     */
    public static final MethodDescription AFTER_RENDER_DESCRIPTION = renderPhaseDescription("afterRender");

    /**
     * Signature for {@link org.apache.tapestry5.runtime.Component#cleanupRender(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.CleanupRender
     * @deprecated Deprecated in Tapestry 5.3, use {@link #CLEANUP_RENDER_DESCRIPTION}
     */
    public static final TransformMethodSignature CLEANUP_RENDER_SIGNATURE = renderPhaseSignature("cleanupRender");

    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#cleanupRender(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.CleanupRender
     * @since 5.3
     */
    public static final MethodDescription CLEANUP_RENDER_DESCRIPTION = renderPhaseDescription("cleanupRender");

    private static TransformMethodSignature renderPhaseSignature(String name)
    {
        return new TransformMethodSignature(Modifier.PUBLIC, "void", name, RENDER_PHASE_METHOD_PARAMETERS, null);
    }

    private static MethodDescription renderPhaseDescription(String name)
    {
        return new MethodDescription(Modifier.PUBLIC, "void", name, RENDER_PHASE_METHOD_PARAMETERS, null, null);
    }

}
