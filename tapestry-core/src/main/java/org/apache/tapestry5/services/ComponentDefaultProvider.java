// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.*;

/**
 * A service that can be injected into a component to provide common defaults for various types of parameters.
 */
public interface ComponentDefaultProvider
{
    /**
     * Computes the default label for the component (which will generally be a {@link Field}).
     *
     * @param resources
     * @return the label, either extracted from the component's container's message catalog, or derived from the
     *         component's {@link ComponentResourcesCommon#getId()}.
     */
    String defaultLabel(ComponentResources resources);

    /**
     * Checks to see if the container of the component (identified by its resources) contains a property matching the
     * component's id. If so, a binding for that property is returned. This is usually the default for a {@link Field}'s
     * value parameter (or equivalent).
     *
     * @param parameterName the name of the parameter
     * @param resources     the resources of the component for which a binding is needed
     * @return the binding, or null if the container does not have a matching property
     */
    Binding defaultBinding(String parameterName, ComponentResources resources);

    /**
     * Gets or creates a value encoder based on the <em>type</em> of the named parameter.  ValueEncoders are cached
     * based on type.
     *
     * @param parameterName the name of the parameter whose type is used to locate a {@link
     *                      org.apache.tapestry5.services.ValueEncoderFactory}
     * @param resources     the resources of the component, from which parameter and its type are extracted
     * @return the value encoder, or null if the type of the parameter is not known
     */
    ValueEncoder defaultValueEncoder(String parameterName, ComponentResources resources);

    /**
     * Provides a translator based on the bound parameter type, if possible.
     *
     * @param parameterName
     * @param resources
     * @return the translator, or null
     * @deprecated Use {@link #defaultTranslatorBinding(String, org.apache.tapestry5.ComponentResources)} instead
     */
    FieldTranslator defaultTranslator(String parameterName, ComponentResources resources);

    /**
     * Provides a binding that itself provides the field translator.
     *
     * @param parameterName
     * @param resources
     * @return binding that provides the {@link org.apache.tapestry5.FieldTranslator}
     */
    Binding defaultTranslatorBinding(String parameterName, ComponentResources resources);

    /**
     * Provides a validator based on the bound parameter type.  If the property type of the parameter is not known, then
     * a no-op validator is returned.
     *
     * @param parameterName
     * @param resources
     * @return the validator, possibly a no-op validator
     * @deprecated Use {@link #defaultValidatorBinding(String, org.apache.tapestry5.ComponentResources)} instead
     */
    FieldValidator defaultValidator(String parameterName, ComponentResources resources);

    /**
     * Provides a binding that itself provides the field translator.
     *
     * @param parameterName
     * @param resources
     * @return binding that provides the {@link org.apache.tapestry5.FieldTranslator}
     */
    Binding defaultValidatorBinding(String parameterName, ComponentResources resources);
}
