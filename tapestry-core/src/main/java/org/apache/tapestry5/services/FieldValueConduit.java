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

package org.apache.tapestry5.services;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ioc.services.TypeCoercer;

/**
 * A callback object that is used to replace storage of a value inside a component field.
 * 
 * @since 5.2.0
 */
public interface FieldValueConduit
{

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

}
