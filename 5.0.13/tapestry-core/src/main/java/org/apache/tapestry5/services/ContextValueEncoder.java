// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.services;

/**
 * Used to convert values used in event contexts to client string representations and back.
 *
 * @See org.apache.tapestry5.ValueEncoder
 * @see org.apache.tapestry5.ioc.services.TypeCoercer
 */
public interface ContextValueEncoder
{
    /**
     * Converts a context value into a client-side string (that will utltimately be encoded into a URL).
     *
     * @param value to convert (may not be null)
     * @return string representation of the value
     * @see org.apache.tapestry5.ValueEncoder#toClient(Object)
     */
    String toClient(Object value);

    /**
     * Converts a client value back into a server-side object.
     *
     * @param requiredType required type to convert the string to
     * @param clientValue  value obtained from context passed from client
     * @return the client value converted or coerced into a server value
     * @see org.apache.tapestry5.ValueEncoder#toValue(String)
     */
    <T> T toValue(Class<T> requiredType, String clientValue);
}
