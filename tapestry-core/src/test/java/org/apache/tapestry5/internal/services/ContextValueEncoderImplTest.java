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
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.testng.annotations.Test;

public class ContextValueEncoderImplTest extends InternalBaseTestCase
{
    @Test
    public void to_client()
    {
        ValueEncoder valueEncoder = mockValueEncoder();
        ValueEncoderSource source = mockValueEncoderSource();

        Long value = 23L;
        String encoded = "twentythree";


        train_getValueEncoder(source, Long.class, valueEncoder);
        train_toClient(valueEncoder, value, encoded);

        replay();

        ContextValueEncoder cve = new ContextValueEncoderImpl(source);

        assertSame(cve.toClient(value), encoded);

        verify();
    }


    @Test
    public void to_value()
    {
        ValueEncoder valueEncoder = mockValueEncoder();
        ValueEncoderSource source = mockValueEncoderSource();

        Long value = 23L;
        String clientValue = "twentythree";


        train_getValueEncoder(source, Long.class, valueEncoder);
        train_toValue(valueEncoder, clientValue, value);

        replay();

        ContextValueEncoder cve = new ContextValueEncoderImpl(source);

        assertSame(cve.toValue(Long.class, clientValue), value);

        verify();
    }

}
