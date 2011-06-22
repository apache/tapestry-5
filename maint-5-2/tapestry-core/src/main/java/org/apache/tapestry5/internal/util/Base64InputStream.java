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

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;

/**
 * An extension of {@link ByteArrayInputStream} that is initialized from a Base64 input stream (rather than from a byte
 * array).
 */
public class Base64InputStream extends ByteArrayInputStream
{
    public Base64InputStream(String base64)
    {
        super(decode(base64));
    }

    private static byte[] decode(String base64)
    {
        byte[] array = base64.getBytes();

        return Base64.decodeBase64(array);
    }
}
