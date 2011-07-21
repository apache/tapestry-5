// Copyright 2007, 2008, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Ensures that all components implement {@link RenderCommand} by delegating to
 * {@link InternalComponentResources#render(org.apache.tapestry5.MarkupWriter, org.apache.tapestry5.runtime.RenderQueue)}.
 * This is also responsible for invoking {@link org.apache.tapestry5.internal.InternalComponentResources#postRenderCleanup()}
 */
public class RenderCommandWorker implements ComponentClassTransformWorker2
{

    private final MethodDescription RENDER_DESCRIPTION = PlasticUtils.getMethodDescription(RenderCommand.class, "render", MarkupWriter.class, RenderQueue.class);

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        // Subclasses don't need to bother, they'll inherit from super-classes.

        if (!support.isRootTransformation())
        {
            return;
        }

        plasticClass.introduceInterface(RenderCommand.class);

        PlasticField resourcesField = plasticClass.introduceField(InternalComponentResources.class, "resources").injectFromInstanceContext();

        plasticClass.introduceMethod(RENDER_DESCRIPTION).delegateTo(resourcesField);

        plasticClass.introduceMethod(TransformConstants.POST_RENDER_CLEANUP_DESCRIPTION).delegateTo(resourcesField);
    }
}
