// Copyright 2010 The Apache Software Foundation
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

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.bindings.LiteralBinding;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.runtime.Component;

/**
 * A facade around {@link Binding} and {@link InternalComponentResources} that is used to instrument
 * fields with the {@link Parameter} annotation.
 * 
 * @since 5.2.0
 */
public interface ParameterConduit
{
    /**
     * Sets the default value for the parameter based on either the current value of the field,
     * or on result from a default method. This is only used if the parameter is not otherwise
     * bound.
     * 
     * @param defaultValue
     *            an object (which will be wrapped as a {@link LiteralBinding}, or
     *            a {@link Binding} instance
     */
    void setDefault(Object defaultValue);

    /**
     * Reads the current value of the parameter (via the {@link Binding}) and uses the
     * {@link TypeCoercer} to convert the actual value to one assignable to the underlying field.
     * The actual read value may be cached.
     * 
     * @throws RuntimeException
     *             if the parameter does not allow null but the current value is null
     * @return current value (possibly null)
     */
    Object get();

    /**
     * Sets the value of the parameter, pushing it through the {@link Binding}.
     * 
     * @param newValue
     */
    void set(Object newValue);

    /**
     * Determines if the parameter is actually bound.
     * 
     * @return
     */
    boolean isBound();

    /**
     * Resets the conduit, clearing any <em>temporarily</em> cached data (from a non-invariant
     * {@link Binding}).
     */
    void reset();

    /**
     * Invoked from the component's {@link Component#containingPageDidLoad()} lifecycle method, to
     * finishing initializing
     * the conduit prior to real use.
     */
    void load();
}
