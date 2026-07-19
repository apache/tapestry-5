// Copyright 2009, 2012, 2026 The Apache Software Foundation
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

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.alerts.AlertManager;
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
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

public class ClientDataEncoderImpl implements ClientDataEncoder
{
    private static final int MIN_PASSPHRASE_LENGTH = 20;

    private final URLEncoder urlEncoder;

    private final Key hmacKey;

    public ClientDataEncoderImpl(URLEncoder urlEncoder,
                                 @Symbol(SymbolConstants.HMAC_PASSPHRASE) String passphrase,
                                 Logger logger,
                                 AlertManager alertManager,
                                 @Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode)
    {
        this.urlEncoder = urlEncoder;

        // TAP5-2834: Also check for minimum length
        if (StringUtils.isBlank(passphrase) || passphrase.length() < MIN_PASSPHRASE_LENGTH)
        {
            String message = String.format(
                    "SymbolConstants.HMAC_PASSPHRASE '%s' has not been configured or is too short (minimum %d characters). " +
                    "This is used to configure hash-based message authentication of Tapestry data stored in forms or in the URL. " +
                    "Your application is less secure and more vulnerable to denial-of-service attacks when this symbol is not properly configured.",
                    SymbolConstants.HMAC_PASSPHRASE, MIN_PASSPHRASE_LENGTH);

            if (productionMode)
            {
                throw new RuntimeException(message);
            }

            alertManager.error(message);
            logger.error(message);

            // TAP5-2834: No longer fall back to application package, as it's easier to guess/deduct.
            // As we disallow an empty passphrase in production, this should only become an issue
            // if run in non-production in a clustered environment. and even then, the easy fix
            // will be configuring a custom passphrase.
            passphrase = UUID.randomUUID().toString();
        }

        hmacKey = new SecretKeySpec(passphrase.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
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

        if (!MessageDigest.isEqual(storedHmacResult.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8)))
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
