// Copyright 2007, 2008, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.util;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

/**
 * A value encoder that can be used for arbitrary Enum types. The enum name is stored as the client side value.
 */
public class EnumValueEncoder<E extends Enum<E>> implements ValueEncoder<E>
{
    private final TypeCoercer typeCoercer;

    private final Class<E> enumType;

    public EnumValueEncoder(TypeCoercer typeCoercer, final Class<E> enumType)
    {
        assert enumType != null;

        this.typeCoercer = typeCoercer;
        this.enumType = enumType;
    }

    public String toClient(E value)
    {
        if (value == null)
            return null;

        return typeCoercer.coerce(value, String.class);
    }

    public E toValue(String clientValue)
    {
        if (InternalUtils.isBlank(clientValue))
            return null;

        return typeCoercer.coerce(clientValue, enumType);
    }

}
