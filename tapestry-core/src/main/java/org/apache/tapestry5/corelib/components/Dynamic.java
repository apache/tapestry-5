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

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.commons.util.AvailableValues;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.services.dynamic.DynamicDelegate;
import org.apache.tapestry5.services.dynamic.DynamicTemplate;
import org.apache.tapestry5.services.dynamic.DynamicTemplateParser;

/**
 * The Dynamic component allows a component to render itself differently at different times, by making use of
 * an external template file.
 *
 * The content of the template file replaces the Dynamic component entirely with one exception: certain elements will be
 * replaced with {@linkplain Block}s passed to the Dynamic component as informal parameters; this is triggered by
 * <strong>id</strong> of the element. When the id attribute has the prefix {@code param:}, the remainder is the name of
 * a Block parameter. There are no limitations on what can appear inside such a Block: text, components, forms, even the
 * {@literal <t:body/>} directive.
 *
 * Dynamic templates emulate how expansions work in standard Tapestry templates: Expansions (the <code>${ ... }</code>
 * syntax) can appear in attribute values or interspersed in element text. This allows container properties, messages,
 * assets, and so forth to be referenced within the external template. It should be noted that such access is quite a
 * bit less efficient than putting such expansions inside a referenced Block, but this should not be a big concern
 * outside of some kind of tight rendering loop.
 * 
 * @since 5.3
 * @see DynamicTemplate
 * @see DynamicTemplateParser
 * @tapestrydoc
 */
@SupportsInformalParameters
public class Dynamic
{
    /** The dynamic template containing the content to be rendered by the component. */
    @Parameter(required = true, allowNull = false, defaultPrefix = BindingConstants.ASSET)
    private DynamicTemplate template;

    @Inject
    private ComponentResources resources;

    private final DynamicDelegate delegate = new DynamicDelegate()
    {
        public ComponentResources getComponentResources()
        {
            return resources;
        }

        public Block getBlock(String name)
        {
            Block result = resources.getBlockParameter(name);

            if (result != null)
                return result;

            throw new UnknownValueException(String.format(
                    "Component %s does not have an informal Block parameter with id '%s'.", resources.getCompleteId(),
                    name), null, null, new AvailableValues("Available Blocks", resources.getInformalParameterNames()));
        }
    };

    RenderCommand beginRender()
    {
        // Probably some room for caching here as well. It shouldn't be necessary to re-create the outermost
        // RenderCommand every time, unless the template has changed from the previous render.
        return template.createRenderCommand(delegate);
    }
}
