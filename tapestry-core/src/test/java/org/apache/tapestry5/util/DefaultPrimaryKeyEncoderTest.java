// Copyright 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.util;

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

import java.util.Arrays;

public class DefaultPrimaryKeyEncoderTest extends InternalBaseTestCase
{
    static class IntStringEncoder extends DefaultPrimaryKeyEncoder<Integer, String>
    {
        public IntStringEncoder()
        {
            super(Integer.class);
        }
    }

    private final int FRED_ID = 1;

    private final String FRED = "FRED";

    private final int BARNEY_ID = 2;

    private final String BARNEY = "BARNEY";

    private final int WILMA_ID = 3;

    private final String WILMA = "WILMA";

    @Test
    public void empty_encoder_has_no_values()
    {
        IntStringEncoder encoder = new IntStringEncoder();

        assertTrue(encoder.getValues().isEmpty());
    }

    @Test
    public void keys_must_be_unique()
    {
        IntStringEncoder encoder = newEncoder();

        try
        {
            encoder.add(FRED_ID, "NewFred");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Key 1 may not be added with value NewFred, as an existing value, FRED, is already present.");
        }
    }

    @Test
    public void extract_key_for_missing_value()
    {
        IntStringEncoder encoder = newEncoder();

        try
        {
            encoder.toKey("BETTY");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Key for value BETTY not found. Available values: BARNEY, FRED, WILMA");
        }
    }

    @Test
    public void value_orderer_maintained()
    {
        IntStringEncoder encoder = newEncoder();

        assertEquals(encoder.getValues(), Arrays.asList(BARNEY, FRED, WILMA));
    }

    @Test
    public void value_to_key()
    {
        IntStringEncoder encoder = newEncoder();

        assertEquals(encoder.toKey(FRED).intValue(), FRED_ID);
        assertEquals(encoder.toKey(BARNEY).intValue(), BARNEY_ID);
    }

    @Test
    public void known_key_to_value()
    {
        IntStringEncoder encoder = newEncoder();

        assertEquals(encoder.toValue(FRED_ID), FRED);
        assertEquals(encoder.toValue(BARNEY_ID), BARNEY);
    }

    @Test
    public void unknown_key_to_value()
    {
        IntStringEncoder encoder = newEncoder();

        assertNull(encoder.toValue(99), null);
    }

    @Test
    public void missing_key_to_provided_object()
    {
        final int bettyId = 5;
        final String betty = "BETTY";

        IntStringEncoder encoder = new IntStringEncoder()
        {
            @Override
            protected String provideMissingObject(Integer key)
            {
                assertEquals(key, new Integer(bettyId));

                return betty;
            }
        };

        assertSame(encoder.toValue(bettyId), betty);
    }

    @Test
    public void set_delete_false_when_nothing_yet_deleted()
    {
        IntStringEncoder encoder = newEncoder();

        assertSame(FRED, encoder.toValue(FRED_ID));

        encoder.setDeleted(false);

        assertEquals(encoder.getValues(), encoder.getAllValues());
    }

    @Test
    public void difference_between_get_values_and_get_all_values()
    {
        IntStringEncoder encoder = newEncoder();

        assertSame(FRED, encoder.toValue(FRED_ID));

        assertFalse(encoder.isDeleted());

        encoder.setDeleted(true);

        assertTrue(encoder.isDeleted());

        assertEquals(encoder.getValues(), Arrays.asList(BARNEY, WILMA));

        assertEquals(encoder.getAllValues(), Arrays.asList(BARNEY, FRED, WILMA));
    }

    @Test
    public void undelete_a_value()
    {
        IntStringEncoder encoder = newEncoder();

        assertSame(FRED, encoder.toValue(FRED_ID));

        encoder.setDeleted(true);
        encoder.setDeleted(false);

        assertEquals(encoder.getValues(), encoder.getAllValues());
    }

    private IntStringEncoder newEncoder()
    {
        IntStringEncoder encoder = new IntStringEncoder();

        encoder.add(BARNEY_ID, BARNEY);
        encoder.add(FRED_ID, FRED);
        encoder.add(WILMA_ID, WILMA);

        return encoder;
    }

}
