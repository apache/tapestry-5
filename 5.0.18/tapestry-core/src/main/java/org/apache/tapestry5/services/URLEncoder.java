//  Copyright 2008 The Apache Software Foundation
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
 * Service used to encode or decode strings that are placed into URLs.  This is used as an alternative to UUEncoding.
 * Alphabetics, numbers and some punctuation ("-", "_", ".", ":") are passed through as is, the "$" character is an
 * escape, followed by either another "$", or by a four digit hex unicode number.  A null input (not a blank input, but
 * actual null) has a special encoding, "$N". Likewise, the blank string has the special encoding "$B".
 */
public interface URLEncoder
{
    /**
     * Given an input value containing any characters, returns the input string, or an encoded version of the string (as
     * outlined above).
     *
     * @param input string to be encoded, which may be null
     * @return encoded version of input
     */
    String encode(String input);

    /**
     * Given a previously encoded string, returns the original input.
     *
     * @param input encoded string (may not be null)
     * @return decoded input
     */
    String decode(String input);
}
