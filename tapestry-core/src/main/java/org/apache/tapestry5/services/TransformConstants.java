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
 * Constants used by implementations {@link ComponentClassTransformWorker2}.
 *
 * Note: render phase methods on transformed components will not be invoked <em>unless</em>
 * {@linkplain org.apache.tapestry5.model.MutableComponentModel#addRenderPhase(Class) the component model is updated to
 * identify the use of the corresponding render phase}. This represents an optimization introduced in Tapestry 5.1.
 */
public final class TransformConstants
{
    // Shared parameters of a whole bunch of lifecycle methods, representing the different
    // component render states.
    private static final String[] RENDER_PHASE_METHOD_PARAMETERS =
            {MarkupWriter.class.getName(), Event.class.getName()};


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
     * Description for {@link org.apache.tapestry5.runtime.Component#postRenderCleanup()}.
     *
     * @since 5.3
     */
    public static final MethodDescription POST_RENDER_CLEANUP_DESCRIPTION = PlasticUtils.getMethodDescription(Component.class, "postRenderCleanup");


    /**
     * Description for {@link org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidDetach()}.
     *
     * @since 5.3
     */
    public static final MethodDescription CONTAINING_PAGE_DID_DETACH_DESCRIPTION = PlasticUtils.getMethodDescription(PageLifecycleListener.class, "containingPageDidDetach");


    /**
     * Description for {@link org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidAttach()}.
     *
     * @since 5.3
     */
    public static final MethodDescription CONTAINING_PAGE_DID_ATTACH_DESCRIPTION = PlasticUtils.getMethodDescription(PageLifecycleListener.class, "containingPageDidAttach");


    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#setupRender(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.SetupRender
     * @since 5.3
     */
    public static final MethodDescription SETUP_RENDER_DESCRIPTION = renderPhaseDescription("setupRender");


    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#beginRender(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeginRender
     * @since 5.3
     */
    public static final MethodDescription BEGIN_RENDER_DESCRIPTION = renderPhaseDescription("beginRender");


    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#beforeRenderTemplate(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeforeRenderTemplate
     * @since 5.3
     */
    public static final MethodDescription BEFORE_RENDER_TEMPLATE_DESCRIPTION = renderPhaseDescription("beforeRenderTemplate");


    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#afterRenderTemplate(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeforeRenderTemplate
     * @since 5.3
     */
    public static final MethodDescription AFTER_RENDER_TEMPLATE_DESCRIPTION = renderPhaseDescription("afterRenderTemplate");


    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#beforeRenderBody(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.BeforeRenderBody
     * @since 5.3
     */
    public static final MethodDescription BEFORE_RENDER_BODY_DESCRIPTION = renderPhaseDescription("beforeRenderBody");


    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#afterRenderBody(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.AfterRenderBody
     * @since 5.3
     */
    public static final MethodDescription AFTER_RENDER_BODY_DESCRIPTION = renderPhaseDescription("afterRenderBody");


    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#afterRender(MarkupWriter, Event)}
     *
     * @see org.apache.tapestry5.annotations.AfterRender
     * @since 5.3
     */
    public static final MethodDescription AFTER_RENDER_DESCRIPTION = renderPhaseDescription("afterRender");


    /**
     * Description for {@link org.apache.tapestry5.runtime.Component#cleanupRender(MarkupWriter, Event)}.
     *
     * @see org.apache.tapestry5.annotations.CleanupRender
     * @since 5.3
     */
    public static final MethodDescription CLEANUP_RENDER_DESCRIPTION = renderPhaseDescription("cleanupRender");


    private static MethodDescription renderPhaseDescription(String name)
    {
        return new MethodDescription(Modifier.PUBLIC, "void", name, RENDER_PHASE_METHOD_PARAMETERS, null, null);
    }

}
