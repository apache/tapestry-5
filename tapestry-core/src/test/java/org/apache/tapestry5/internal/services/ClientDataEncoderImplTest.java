// Copyright 2012, 2020, 2026 The Apache Software Foundation
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

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.ClientDataSink;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientDataEncoderImplTest
{
    private static final String VALID_PASSPHRASE = "a sufficiently long passphrase";

    private String convertToClientData(ClientDataEncoder cde, Object input) throws IOException
    {
        ClientDataSink sink = cde.createSink();
        ObjectOutputStream stream = sink.getObjectOutputStream();
        stream.writeObject(input);
        stream.close();
        return sink.getClientData();
    }

    private void tryEncodeAndDecode(ClientDataEncoder cde) throws Exception
    {
        String input = "The current time is " + new java.util.Date();
        String clientData = convertToClientData(cde, input);
        ObjectInputStream stream = cde.decodeClientData(clientData);
        Object output = stream.readObject();
        assertNotSame(input, output);
        assertEquals(input, output);
    }

    private String extractData(String encoded)
    {
        int colonx = encoded.indexOf(':');
        return encoded.substring(colonx + 1);
    }

    @Test
    void blank_passphrase_warns_in_development() throws Exception
    {
        Logger logger = EasyMock.mock(Logger.class);
        AlertManager alertManager = EasyMock.mock(AlertManager.class);

        logger.error(EasyMock.isA(String.class));
        alertManager.error(EasyMock.isA(String.class));

        EasyMock.replay(logger, alertManager);

        ClientDataEncoder cde = new ClientDataEncoderImpl(null, "", logger, alertManager, false);
        tryEncodeAndDecode(cde);

        EasyMock.verify(logger, alertManager);
    }

    @Test
    void whitespace_passphrase_warns_in_development() throws Exception
    {
        Logger logger = EasyMock.mock(Logger.class);
        AlertManager alertManager = EasyMock.mock(AlertManager.class);

        logger.error(EasyMock.isA(String.class));
        alertManager.error(EasyMock.isA(String.class));

        EasyMock.replay(logger, alertManager);

        ClientDataEncoder cde = new ClientDataEncoderImpl(null, "   ", logger, alertManager, false);
        tryEncodeAndDecode(cde);

        EasyMock.verify(logger, alertManager);
    }

    @Test
    void short_passphrase_warns_in_development() throws Exception
    {
        Logger logger = EasyMock.mock(Logger.class);
        AlertManager alertManager = EasyMock.mock(AlertManager.class);

        logger.error(EasyMock.isA(String.class));
        alertManager.error(EasyMock.isA(String.class));

        EasyMock.replay(logger, alertManager);

        ClientDataEncoder cde = new ClientDataEncoderImpl(null, "short", logger, alertManager, false);
        tryEncodeAndDecode(cde);

        EasyMock.verify(logger, alertManager);
    }

    @Test
    void blank_passphrase_fails_in_production()
    {
        assertThrows(RuntimeException.class,
                () -> new ClientDataEncoderImpl(null, "", null, null, true));
    }

    @Test
    void short_passphrase_fails_in_production()
    {
        assertThrows(RuntimeException.class,
                () -> new ClientDataEncoderImpl(null, "too short", null, null, true));
    }

    @Test
    void valid_passphrase_works_in_production() throws Exception
    {
        ClientDataEncoder cde = new ClientDataEncoderImpl(null, VALID_PASSPHRASE, null, null, true);
        tryEncodeAndDecode(cde);
    }

    @Test
    void no_logged_error_with_sufficient_passphrase() throws Exception
    {
        ClientDataEncoder cde = new ClientDataEncoderImpl(null, "Testing, Testing, 1.., 2.., 3...", null, null, false);
        tryEncodeAndDecode(cde);
    }

    @Test
    void passphrase_affects_encoded_output() throws Exception
    {
        ClientDataEncoder first = new ClientDataEncoderImpl(null, "first-passphrase-long-enough", null, null, false);
        ClientDataEncoder second = new ClientDataEncoderImpl(null, "different-passphrase-long-enough", null, null, false);

        String input = "current time millis is " + System.currentTimeMillis() + " ms";

        String output1 = convertToClientData(first, input);
        String output2 = convertToClientData(second, input);

        assertNotEquals(output1, output2);
        assertEquals(extractData(output1), extractData(output2));
    }

    @Test
    void decode_with_missing_hmac_prefix_is_a_failure() throws Exception
    {
        ClientDataEncoder cde = new ClientDataEncoderImpl(null, VALID_PASSPHRASE, null, null, false);

        assertThrows(IllegalArgumentException.class,
                () -> cde.decodeClientData("so completely invalid"));
    }

    @Test
    void incorrect_hmac_is_detected() throws Exception
    {
        ClientDataEncoder first = new ClientDataEncoderImpl(null, "first-passphrase-long-enough", null, null, false);
        ClientDataEncoder second = new ClientDataEncoderImpl(null, "different-passphrase-long-enough", null, null, false);

        String input = "current time millis is " + System.currentTimeMillis() + " ms";
        String encoded = convertToClientData(first, input);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> second.decodeClientData(encoded));
        assertTrue(ex.getMessage().contains("HMAC signature does not match"));
    }

    @Test
    void check_for_eof() throws Exception
    {
        ClientDataEncoder cde = new ClientDataEncoderImpl(null, "hmac passphrase is valid here", null, null, false);

        ClientDataSink sink = cde.createSink();
        ObjectOutputStream os = sink.getObjectOutputStream();

        List<String> names = List.of("fred", "barney", "wilma");
        for (String name : names)
        {
            os.writeObject(name);
        }
        os.close();

        ObjectInputStream ois = cde.decodeClientData(sink.getClientData());
        for (String name : names)
        {
            assertEquals(name, ois.readObject());
        }

        assertThrows(EOFException.class, ois::readObject);
    }
}
