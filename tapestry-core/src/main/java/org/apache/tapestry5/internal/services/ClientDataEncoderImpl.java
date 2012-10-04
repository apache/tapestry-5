// Copyright 2009, 2012 The Apache Software Foundation
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

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.util.Base64InputStream;
import org.apache.tapestry5.internal.util.MacOutputStream;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.ClientDataSink;
import org.apache.tapestry5.services.URLEncoder;
import org.slf4j.Logger;

import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.zip.GZIPInputStream;

public class ClientDataEncoderImpl implements ClientDataEncoder
{
    private final URLEncoder urlEncoder;

    private final Key hmacKey;

    public ClientDataEncoderImpl(URLEncoder urlEncoder, @Symbol(SymbolConstants.HMAC_PASSPHRASE) String passphrase, Logger logger) throws UnsupportedEncodingException
    {
        this.urlEncoder = urlEncoder;

        if (passphrase.equals(""))
        {
            logger.error(String.format("The symbol '%s' has not been configured. " +
                    "This is used to configure hash-based message authentication of Tapestry data stored in forms, or in the URL. " +
                    "You application is less secure, and more vulnerable to denial-of-service attacks, when this symbol is left unconfigured.",
                    SymbolConstants.HMAC_PASSPHRASE));

            // Errors at lower levels if the passphrase is empty, so override the parameter to set a default value.
            passphrase = "DEFAULT";
        }

        hmacKey = new SecretKeySpec(passphrase.getBytes("UTF8"), "HmacSHA1");
    }

    public ClientDataSink createSink()
    {
        try
        {
            return new ClientDataSinkImpl(urlEncoder, hmacKey);
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public ObjectInputStream decodeClientData(String clientData)
    {
        // The clientData is Base64 that's been gzip'ed (i.e., this matches
        // what ClientDataSinkImpl does).

        int colonx = clientData.indexOf(':');

        if (colonx < 0)
        {
            throw new IllegalArgumentException("Client data must be prefixed with its HMAC code.");
        }

        // Extract the string presumably encoded by the server using the secret key.

        String storedHmacResult = clientData.substring(0, colonx);

        String clientStream = clientData.substring(colonx + 1);

        try
        {
            Base64InputStream b64in = new Base64InputStream(clientStream);

            validateHMAC(storedHmacResult, b64in);

            // After reading it once to validate, reset it for the actual read (which includes the GZip decompression).

            b64in.reset();

            BufferedInputStream buffered = new BufferedInputStream(new GZIPInputStream(b64in));

            return new ObjectInputStream(buffered);
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void validateHMAC(String storedHmacResult, Base64InputStream b64in) throws IOException
    {
        MacOutputStream macOs = MacOutputStream.streamFor(hmacKey);

        TapestryInternalUtils.copy(b64in, macOs);

        macOs.close();

        String actual = macOs.getResult();

        if (!storedHmacResult.equals(actual))
        {
            throw new IOException("Client data associated with the current request appears to have been tampered with " +
                    "(the HMAC signature does not match).");
        }
    }

    public ObjectInputStream decodeEncodedClientData(String clientData) throws IOException
    {
        return decodeClientData(urlEncoder.decode(clientData));
    }
}
