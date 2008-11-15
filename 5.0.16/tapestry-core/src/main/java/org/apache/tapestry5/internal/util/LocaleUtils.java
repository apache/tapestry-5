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

package org.apache.tapestry5.internal.util;

import java.util.Locale;

/**
 * Contains code borrowed from <a href="http://commons.apache.org/lang/">commons-lang</a>.
 */
public class LocaleUtils
{
    /**
     * <p>Converts a String to a Locale.</p> <p/> <p>This method takes the string format of a locale and creates the
     * locale object from it.</p> <p/>
     * <pre>
     *   LocaleUtils.toLocale("en")         = new Locale("en", "")
     *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
     *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
     * </pre>
     * <p/> <p>(#) The behaviour of the JDK variant constructor changed between JDK1.3 and JDK1.4. In JDK1.3, the
     * constructor upper cases the variant, in JDK1.4, it doesn't. Thus, the result from getVariant() may vary depending
     * on your JDK.</p> <p/> <p>This method validates the input strictly. The language code must be lowercase. The
     * country code must be uppercase. The separator must be an underscore. The length must be correct. </p>
     *
     * @param input the locale String to convert, null returns null
     * @return a Locale, null if null input
     * @throws IllegalArgumentException if the string is an invalid format
     */
    public static Locale toLocale(String input)
    {
        if (input == null)
            return null;

        int len = input.length();
        if (len != 2 && len != 5 && len < 7)
            fail(input);

        char ch0 = input.charAt(0);
        char ch1 = input.charAt(1);

        if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z')
            fail(input);

        if (len == 2)
            return new Locale(input, "");

        if (input.charAt(2) != '_')
            fail(input);

        char ch3 = input.charAt(3);
        if (ch3 == '_')
            return new Locale(input.substring(0, 2), "", input.substring(4));

        char ch4 = input.charAt(4);
        if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z')
            fail(input);

        if (len == 5)
            return new Locale(input.substring(0, 2), input.substring(3, 5));

        if (input.charAt(5) != '_')
            fail(input);

        return new Locale(input.substring(0, 2), input.substring(3, 5), input.substring(6));
    }

    private static void fail(String input)
    {
        throw new IllegalArgumentException(String.format("Unable to convert '%s' to a Locale instance.", input));
    }

}
