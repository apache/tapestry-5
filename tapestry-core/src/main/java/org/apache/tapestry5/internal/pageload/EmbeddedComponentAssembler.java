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

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.model.ComponentModel;

/**
 * Encapsulates logic related to assembling an embedded component within a {@link org.apache.tapestry5.internal.pageload.ComponentAssembler}.
 */
interface EmbeddedComponentAssembler
{
    void addInstanceMixin(ComponentModel mixinModel);

    /**
     * Creates a binder that can later be used to bind the parameter. The parameter name may be unqualified ("value") or
     * have a mixin prefix ("mymixin.value").  In the former case, the correct mixin is located (though the more typical
     * case is to bind a parameter of the component itself, not a parameter of a mixin attached to the component). In
     * the latter case, the mixinId is validated (to ensure it exists).
     * <p/>
     * If the name of the parameter does not match a formal parameter of the component (or mixin) and the component (or
     * mixin) does not support informal parameters, then null is returned.
     *
     * @param parameterName        simple or qualified parameter name
     * @param parameterValue       value of parameter (possibly having a binding prefix)
     * @param defaultBindingPrefix default binding prefix to use if the parameter is informal
     * @return object that can bind the parameter once the container and component have been instantiated, or null
     */
    ParameterBinder createBinder(String parameterName, String parameterValue, String defaultBindingPrefix);

    /**
     * Creates a ParameterBinding where the binding is already instantiated. Follows the same logic as {@link
     * #createBinder(String, String, String)} in terms of finding the correct mixin and parameter name.
     *
     * @param parameterName simple or qualified parameter name
     * @param binding       binding for parameter
     * @return object that can perform the binding, or null
     */
    ParameterBinder createBinder(String parameterName, Binding binding);

    /**
     * Checks to see if the parameter name  has been bound.
     */
    boolean isBound(String parameterName);

    /**
     * Marks the parameter name as bound. This is necessary to keep template bindings from overriding bindings in the
     * {@link org.apache.tapestry5.annotations.Component} annotation (even inherited bindings).
     */
    void setBound(String parameterName);
}
