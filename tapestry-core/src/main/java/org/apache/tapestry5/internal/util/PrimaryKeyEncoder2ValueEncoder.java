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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.PrimaryKeyEncoder;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.util.DefaultPrimaryKeyEncoder;

import java.io.Serializable;

/**
 * This is a key part of the plan to eliminate {@link org.apache.tapestry5.PrimaryKeyEncoder}.
 *
 * @since 5.1.0.0
 */
@SuppressWarnings({ "unchecked" })
public class PrimaryKeyEncoder2ValueEncoder implements Coercion<PrimaryKeyEncoder, ValueEncoder>
{
    // The magic of proxies: a coercion within TypeCoercer can use TypeCoercer as part of its job!
    private final TypeCoercer coercer;

    public PrimaryKeyEncoder2ValueEncoder(TypeCoercer coercer)
    {
        this.coercer = coercer;
    }

    public ValueEncoder coerce(final PrimaryKeyEncoder input)
    {
        final Class keyType = input.getKeyType();

        if (keyType == null)
        {
            String message = String.format("Unable to extract primary key type from %s. " +
                    "This represents a change from Tapestry 5.0 to Tapestry 5.1.", input);

            if (input instanceof DefaultPrimaryKeyEncoder)
                message +=
                        " Class DefaultPrimaryKeyEncoder now includes a constructor for specifying the key type. " +
                                "You should change the code that instantiates the encoder.";
            else
                message += " You should ensure that the getKeyType() method returns the correct Class.";

            throw new RuntimeException(message);
        }

        return new ValueEncoder()
        {
            public String toClient(Object value)
            {
                Object key = input.toKey(value);

                return coercer.coerce(key, String.class);
            }

            public Object toValue(String clientValue)
            {
                Serializable key = (Serializable) coercer.coerce(clientValue, keyType);

                return input.toValue(key);
            }

            @Override
            public String toString()
            {
                return String.format("<ValueEncoder coercion wrapper around PrimaryKeyEncoder[%s]>",
                                     keyType.getName());
            }
        };
    }
}
