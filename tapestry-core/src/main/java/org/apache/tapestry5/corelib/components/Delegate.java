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

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.runtime.Component;

/**
 * A component that does not do any rendering of its own, but will delegate to some other object that can do rendering.
 * This other object may be a component or a {@link Block} (among other things).
 *
 * This component may also be used to create inline components. For each informal parameter the value will be stored as a 
 * render variable. To create an inline component, create a block
 * and use Delegate multiple times in the template to render the block passing parameters to Delegate. In the block body
 * reference the render variables using the "var:" binding prefix and the name of the parameter.
 *
 * Note that the default binding prefix for informal parameter values is "literal".
 * 
 * @tapestrydoc
 */
@SupportsInformalParameters
public class Delegate
{
    /**
     * The object which will be rendered in place of the Delegate component. This is typically a specific component
     * instance, or a {@link Block}.
     */
    @Parameter(required = true)
    private Object to;

    @Inject private ComponentResources resources;
    @InjectContainer private Component container;
    
    Object beginRender()
    {
        for(String name : resources.getInformalParameterNames()) {
            Object value = resources.getInformalParameter(name, Object.class);
            container.getComponentResources().storeRenderVariable(name, value);
        }
        
        return to;
    }
}
