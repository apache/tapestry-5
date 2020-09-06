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
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.plastic.FieldConduit;

/**
 * A facade around {@link Binding} and {@link InternalComponentResources} that is used to instrument
 * fields with the {@link Parameter} annotation. Extends {@link FieldConduit} so that
 * the get() method implicitly coerces the value to the field's type.
 *
 * {@link #get(Object, org.apache.tapestry5.plastic.InstanceContext)} will read from the underlying {@link Binding} and used the {@link TypeCoercer} coerce the value to the
 * parameter field's type. get() also includes a null value check (as per {@link Parameter#allowNull()}.
 *
 * {@link #set(Object, org.apache.tapestry5.plastic.InstanceContext, Object)} pushes the value into the binding.
 *
 * @since 5.2.0
 */
public interface ParameterConduit extends FieldConduit<Object>
{
    /**
     * Determines if the parameter is actually bound.
     *
     * @return true if bound
     */
    boolean isBound();

    /**
     * Resets the conduit, clearing any <em>temporarily</em> cached data (from a non-invariant {@link Binding}).
     */
    void reset();
}
