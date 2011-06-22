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

import java.io.ByteArrayOutputStream;

/**
 * An extension of {@link ByteArrayOutputStream} that allows the final byte array to be converted to a Base64 string.
 */
public final class Base64OutputStream extends ByteArrayOutputStream
{
    public String toBase64()
    {
        byte[] binary = toByteArray();

        byte[] base64 = Base64.encodeBase64(binary);

        return new String(base64);
    }
}
