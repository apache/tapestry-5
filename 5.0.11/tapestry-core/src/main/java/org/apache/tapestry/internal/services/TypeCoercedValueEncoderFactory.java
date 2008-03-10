// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.services.ValueEncoderFactory;

/**
 * Provides {@link org.apache.tapestry.ValueEncoder} instances that are backed by the {@link
 * org.apache.tapestry.ioc.services.TypeCoercer} service.
 */
public class TypeCoercedValueEncoderFactory implements ValueEncoderFactory<Object>
{
    private final TypeCoercer _typeCoercer;

    public TypeCoercedValueEncoderFactory(TypeCoercer typeCoercer)
    {
        _typeCoercer = typeCoercer;
    }

    public ValueEncoder<Object> create(final Class<Object> type)
    {
        return new ValueEncoder<Object>()
        {
            public String toClient(Object value)
            {
                return _typeCoercer.coerce(value, String.class);
            }

            public Object toValue(String clientValue)
            {
                return _typeCoercer.coerce(clientValue, type);
            }
        };
    }
}
