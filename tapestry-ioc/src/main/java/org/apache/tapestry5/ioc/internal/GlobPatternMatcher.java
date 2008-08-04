// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import java.util.regex.Pattern;

/**
 * Used when matching identifiers.  In the early days of T5 IoC, matching was based on shell-style glob matches (a '*'
 * could represent zero or more characters).  But that was limiting so now we check to see if the provided pattern looks
 * like a glob (just characters and asterisks, for compatibility with older code) and, if not, we assume it is a regular
 * expression.
 */
public class GlobPatternMatcher
{
    private final Pattern pattern;

    private final static Pattern oldStyleGlob =
            Pattern.compile("[a-z\\*]+", Pattern.CASE_INSENSITIVE);

    public GlobPatternMatcher(String pattern)
    {
        this.pattern = compilePattern(pattern);
    }

    private static Pattern compilePattern(String pattern)
    {
        return Pattern.compile(createRegexpFromGlob(pattern), Pattern.CASE_INSENSITIVE);
    }

    private static String createRegexpFromGlob(String pattern)
    {
        return oldStyleGlob.matcher(pattern).matches()
               ? pattern.replace("*", ".*")
               : pattern;
    }


    public boolean matches(String input)
    {
        return pattern.matcher(input).matches();
    }
}
