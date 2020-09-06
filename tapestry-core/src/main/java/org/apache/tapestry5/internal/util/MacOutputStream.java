// Copyright 2012-2013 The Apache Software Foundation
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

import org.apache.commons.codec.binary.Base64;
import org.apache.tapestry5.commons.util.ExceptionUtils;

import javax.crypto.Mac;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;

/**
 * An output stream that wraps around a {@link Mac} (message authentication code algorithm).  This is currently
 * used for symmetric (private) keys, but in theory could be used with assymetric (public/private) keys.
 *
 * @since 5.3.6
 */
public class MacOutputStream extends OutputStream
{
    private final Mac mac;

    public static MacOutputStream streamFor(Key key) throws IOException
    {
        try
        {
            Mac mac = Mac.getInstance(key.getAlgorithm());
            mac.init(key);

            return new MacOutputStream(mac);
        } catch (Exception ex)
        {
            throw new IOException("Unable to create MacOutputStream: " + ExceptionUtils.toMessage(ex), ex);
        }
    }

    public MacOutputStream(Mac mac)
    {
        assert mac != null;

        this.mac = mac;
    }

    /**
     * Should only be invoked once, immediately after this stream is closed; it generates the final MAC result, and
     * returns it as a Base64 encoded string.
     *
     * @return Base64 encoded MAC result
     */
    public String getResult()
    {
        byte[] result = mac.doFinal();

        return new String(Base64.encodeBase64(result));
    }

    @Override
    public void write(int b) throws IOException
    {
        mac.update((byte) b);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        mac.update(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        mac.update(b, off, len);
    }
}
