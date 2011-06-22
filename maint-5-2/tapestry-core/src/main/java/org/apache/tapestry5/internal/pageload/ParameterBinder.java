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
import org.apache.tapestry5.internal.structure.ComponentPageElement;

/**
 * Encapsulates the result of a parameter lookup, i.e., the conversion from qualified or unqualified parameter name to
 * mixin id (or null) and parameter name, as well as the default binding prefix for the parameter.
 *
 * @see org.apache.tapestry5.internal.pageload.EmbeddedComponentAssembler#createParameterBinder(String)
 */
interface ParameterBinder
{
    /**
     * Bindings the parameter of the element.   The name (and optionally mixinid) of the parameter is determined when
     * the ParameterBinder is created.
     *
     * @param element page element to bind
     * @param binding binding for the parameter
     */
    void bind(ComponentPageElement element, Binding binding);

    /**
     * Returns the correct default binding prefix to use for this parameter, which is either the {@linkplain
     * org.apache.tapestry5.model.ParameterModel#getDefaultBindingPrefix() default binding prefix configured for the
     * parameter}, or the meta-default (when binding an informal parameter). A specific binding of a parameter may
     * always override the default binding prefix, however it is calculated.
     *
     * @param metaDefault
     * @return binding prefix
     */
    String getDefaultBindingPrefix(String metaDefault);
}
