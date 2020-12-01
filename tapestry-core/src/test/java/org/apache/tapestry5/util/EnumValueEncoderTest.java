// Copyright 2007, 2011 The Apache Software Foundation
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.commons.internal.services.TypeCoercerImpl;
import org.apache.tapestry5.commons.services.Coercion;
import org.apache.tapestry5.commons.services.CoercionTuple;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;


public class EnumValueEncoderTest extends InternalBaseTestCase
{
    private enum Stooge
    {
        MOE, LARRY, CURLY_JOE
    }

    @Test
    // TAP5-1331
    public void valid_values_can_be_retrieved_from_exception()
    {
        TypeCoercer typeCoercer = getService(TypeCoercer.class);
        EnumValueEncoder<Stooge> encoder = new EnumValueEncoder<Stooge>(typeCoercer, Stooge.class);
        try
        {
          encoder.toValue("Foo");
          fail();
        } catch (RuntimeException e){
          assertTrue(e.getCause() instanceof UnknownValueException);
          UnknownValueException cause = (UnknownValueException) e.getCause();
          List<String> availableValues = cause.getAvailableValues().getValues();
          assertTrue(availableValues.contains("MOE"));
          assertTrue(availableValues.contains("LARRY"));
          assertTrue(availableValues.contains("CURLY_JOE"));
        }
    }

    @Test
    // TAP5-2496
    public void roundtrip_with_custom_coercer()
    {

        CoercionTuple<Stooge, String> stoogeToString = CoercionTuple.create(Stooge.class, String.class, new Coercion<Stooge, String>(){
            @Override
            public String coerce(Stooge input) {
                return String.valueOf(input.ordinal());
            }
        });

        CoercionTuple<String, Stooge> stringToStooge = CoercionTuple.create(String.class, Stooge.class, new Coercion<String, Stooge>(){

            @Override
            public Stooge coerce(String input) {
                return Stooge.values()[Integer.parseInt(input)];
            }

        });

        Map<CoercionTuple.Key, CoercionTuple> map = new HashMap<>();
        map.put(stoogeToString.getKey(), stoogeToString);
        map.put(stringToStooge.getKey(), stringToStooge);
        TypeCoercer typeCoercer =  new TypeCoercerImpl(map);


        EnumValueEncoder<Stooge> encoder = new EnumValueEncoder<Stooge>(typeCoercer, Stooge.class);
        Stooge serverValue = Stooge.LARRY;
        String clientValue = encoder.toClient(serverValue);
        Stooge convertedBack = encoder.toValue(clientValue);
        assertEquals(convertedBack, serverValue);

    }
}
