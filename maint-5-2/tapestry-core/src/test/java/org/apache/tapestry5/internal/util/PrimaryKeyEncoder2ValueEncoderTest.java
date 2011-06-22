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
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.util.DefaultPrimaryKeyEncoder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.List;

public class PrimaryKeyEncoder2ValueEncoderTest extends InternalBaseTestCase
{

    private PrimaryKeyEncoder2ValueEncoder coercion;

    @BeforeClass
    public void setup()
    {
        TypeCoercer coercer = getService(TypeCoercer.class);

        coercion = new PrimaryKeyEncoder2ValueEncoder(coercer);
    }

    @Test
    public void key_type_is_known()
    {
        PrimaryKeyEncoder pke = newMock(PrimaryKeyEncoder.class);

        Object value = new Object();
        Long primaryKey = new Long(99);

        expect(pke.getKeyType()).andReturn(Long.class);

        expect(pke.toKey(value)).andReturn(primaryKey);

        expect(pke.toValue(primaryKey)).andReturn(value);

        replay();

        ValueEncoder ve = coercion.coerce(pke);

        assertEquals(ve.toClient(value), "99");
        assertEquals(ve.toValue("99"), value);

        verify();
    }

    @Test
    public void unknown_key_type()
    {
        PrimaryKeyEncoder pke = new PrimaryKeyEncoder()
        {
            public Serializable toKey(Object value)
            {
                return null;
            }

            public void prepareForKeys(List keys)
            {
            }

            public Object toValue(Serializable key)
            {
                return null;
            }

            public Class getKeyType()
            {
                return null;
            }

            @Override
            public String toString()
            {
                return "<Dummy>";
            }
        };

        try
        {
            coercion.coerce(pke);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "Unable to extract primary key type from <Dummy>.",
                                  "You should ensure that the getKeyType() method returns the correct Class.");
        }
    }

    @Test
    public void unknown_key_type_for_default_pke()
    {
        try
        {
            coercion.coerce(new DefaultPrimaryKeyEncoder());
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "Class DefaultPrimaryKeyEncoder now includes a constructor for specifying the key type.");
        }
    }


}
