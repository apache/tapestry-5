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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.apache.tapestry5.services.ValueEncoderSource;

public class ContextValueEncoderImpl implements ContextValueEncoder
{
    private final ValueEncoderSource valueEncoderSource;

    public ContextValueEncoderImpl(ValueEncoderSource valueEncoderSource)
    {
        this.valueEncoderSource = valueEncoderSource;
    }

    public String toClient(Object value)
    {
        Defense.notNull(value, "value");

        ValueEncoder encoder = valueEncoderSource.getValueEncoder(value.getClass());

        return encoder.toClient(value);
    }


    public <T> T toValue(Class<T> requiredType, String clientValue)
    {
        Defense.notNull(requiredType, "requiredType");

        ValueEncoder<T> encoder = valueEncoderSource.getValueEncoder(requiredType);

        return encoder.toValue(clientValue);
    }
}
