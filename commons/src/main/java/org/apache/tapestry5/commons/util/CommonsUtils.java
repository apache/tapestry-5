// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.commons.util;

import java.util.regex.Pattern;

/**
 * Some utility methods used in different Tapestry subprojects.
 */
public class CommonsUtils
{

    private static final String SLASH = "/";

    private static final Pattern SLASH_PATTERN = Pattern.compile(SLASH);

    private static final Pattern COMMA_PATTERN = Pattern.compile("\\s*,\\s*");
    
    /**
     * Splits a path at each slash.
     */
    public static String[] splitPath(String path)
    {
        return SLASH_PATTERN.split(path);
    }

    /**
     * Splits a value around commas. Whitespace around the commas is removed, as is leading and trailing whitespace.
     *
     * @since 5.1.0.0
     */
    public static String[] splitAtCommas(String value)
    {
        if (isBlank(value))
            return EMPTY_STRING_ARRAY;

        return COMMA_PATTERN.split(value.trim());
    }

    /**
     * Returns true if the input is null, or is a zero length string (excluding leading/trailing whitespace).
     */
    
    public static boolean isBlank(String input)
    {
        return input == null || input.length() == 0 || input.trim().length() == 0;
    }

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

}
