// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry5.ValueEncoder;

/**
 * Passes the string value from the server to the client and vice-versa without any translation.
 */
public class StringValueEncoder implements ValueEncoder<String>
{
    public String toClient(String value)
    {
        return value;
    }

    public String toValue(String clientValue)
    {
        return clientValue;
    }
}
