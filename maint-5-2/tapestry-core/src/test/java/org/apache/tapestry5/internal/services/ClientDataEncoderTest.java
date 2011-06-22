// Copyright 2006, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.ClientDataSink;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class ClientDataEncoderTest extends InternalBaseTestCase
{
    private ClientDataEncoder encoder;

    @BeforeClass
    public void setup()
    {
        encoder = getService(ClientDataEncoder.class);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void round_trip_is_equal() throws Exception
    {
        Map input = CollectionFactory.newMap();

        input.put("fred", "flintstone");
        input.put("barney", "rubble");

        ClientDataSink sink = encoder.createSink();

        sink.getObjectOutputStream().writeObject(input);

        String clientData = sink.getClientData();

        ObjectInputStream ois = encoder.decodeClientData(clientData);

        Map output = (Map) ois.readObject();

        assertEquals(output, input);
        assertNotSame(output, input);
    }

    @Test
    public void checks_for_eof() throws Exception
    {
        String[] values = { "fred", "barney", "wilma" };

        ClientDataSink sink = encoder.createSink();

        ObjectOutputStream os = sink.getObjectOutputStream();

        for (String value : values)
            os.writeObject(value);

        os.close();

        String clientData = sink.getClientData();

        ObjectInputStream ois = encoder.decodeClientData(clientData);

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
