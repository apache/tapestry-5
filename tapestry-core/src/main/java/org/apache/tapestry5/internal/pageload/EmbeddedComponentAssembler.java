// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.pageload;

import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.ioc.Locatable;

/**
 * Encapsulates logic related to assembling an embedded component within a {@link org.apache.tapestry5.internal.pageload.ComponentAssembler}.
 */
interface EmbeddedComponentAssembler extends Locatable
{
    /**
     * Returns the assembler responsible for creating instances of this embedded component.
     */
    ComponentAssembler getComponentAssembler();

    /**
     * Creates a binder that can later be used to bind the parameter. The parameter name may be unqualified ("value") or
     * have a mixin prefix ("mymixin.value").  In the former case, the correct mixin is located (though the more typical
     * case is to bind a parameter of the component itself, not a parameter of a mixin attached to the component). In
     * the latter case, the mixinId is validated (to ensure it exists).
     * <p/>
     * If the name of the parameter does not match a formal parameter of the component (or mixin) and the component (or
     * mixin) does not support informal parameters, then null is returned.
     * <p/>
     * This method should only be called at page-assembly time as it requires some data that is collected during
     * ComponentAssembly construction in order to handle published parameters of embedded components.
     *
     * @param parameterName simple or qualified parameter name
     * @return object that can bind the parameter
     */
    ParameterBinder createParameterBinder(String parameterName);

    /**
     * Checks to see if the parameter name  has been bound.
     */
    boolean isBound(String parameterName);

    /**
     * Marks the parameter name as bound. This is necessary to keep template bindings from overriding bindings in the
     * {@link org.apache.tapestry5.annotations.Component} annotation (even inherited bindings).
     */
    void setBound(String parameterName);


    /**
     * Adds mixins to the newly created embedded element.
     *
     * @param newElement new element requiring mixins
     */
    void addMixinsToElement(ComponentPageElement newElement);
}
