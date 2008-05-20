// Copyright 2006 The Apache Software Foundation
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

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.EOFException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Tests for {@link Base64InputStream} and {@link Base64OutputStream}, etc.
 */
public class Base64Tests extends Assert
{
    @SuppressWarnings("unchecked")
    @Test
    public void round_trip_is_equal() throws Exception
    {
        Map input = newMap();

        input.put("fred", "flintstone");
        input.put("barney", "rubble");

        Base64OutputStream bos = new Base64OutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeObject(input);

        oos.close();

        String base64 = bos.toBase64();

        InputStream is = new Base64InputStream(base64);
        ObjectInputStream ois = new ObjectInputStream(is);

        Map output = (Map) ois.readObject();

        assertEquals(output, input);
        assertNotSame(output, input);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void round_trip_is_equal_via_object_wrappers() throws Exception
    {
        Map input = newMap();

        input.put("fred", "flintstone");
        input.put("barney", "rubble");

        Base64ObjectOutputStream os = new Base64ObjectOutputStream();

        os.writeObject(input);

        os.close();

        String base64 = os.toBase64();

        ObjectInputStream ois = new Base64ObjectInputStream(base64);

        Map output = (Map) ois.readObject();

        assertEquals(output, input);
        assertNotSame(output, input);
    }

    @Test
    public void checks_for_eof() throws Exception
    {
        String[] values = { "fred", "barney", "wilma" };

        Base64ObjectOutputStream os = new Base64ObjectOutputStream();

        for (String value : values)
            os.writeObject(value);

        os.close();

        String base64 = os.toBase64();

        ObjectInputStream ois = new Base64ObjectInputStream(base64);

        for (int i = 0; i < 3; i++)
        {
            String value = (String) ois.readObject();

            assertEquals(value, values[i]);
        }

        try
        {
            ois.readObject();
            fail("Unreachable.");
        }
        catch (EOFException ex)
        {
            // Expected.
        }

    }
}
