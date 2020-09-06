// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.services.ValueLabelProvider;

/**
 * Uses the {@link TypeCoercer} to convert an arbitrary value to a string.
 *
 * @since 5.4
 */
public class DefaultValueLabelProvider implements ValueLabelProvider<Object>
{
    private final TypeCoercer coercer;

    public DefaultValueLabelProvider(TypeCoercer coercer)
    {
        this.coercer = coercer;
    }

    public String getLabel(Object value)
    {
        return coercer.coerce(value, String.class);
    }

}
