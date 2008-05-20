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

package org.apache.tapestry5.ioc.internal.util;

import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Used in a wide variety of resource searches. Generates a series of name variations from a path (which must include a
 * suffix) and locale.
 * <p/>
 * This class is not threadsafe.
 */
public class LocalizedNameGenerator implements Iterator<String>, Iterable<String>
{
    private final int baseNameLength;

    private final String suffix;

    private final StringBuilder builder;

    private final String language;

    private final String country;

    private final String variant;

    private int state;

    private int prevState;

    private static final int INITIAL = 0;

    private static final int LCV = 1;

    private static final int LC = 2;

    private static final int LV = 3;

    private static final int L = 4;

    private static final int BARE = 5;

    private static final int EXHAUSTED = 6;

    public LocalizedNameGenerator(String path, Locale locale)
    {
        int dotx = path.lastIndexOf('.');

        // When there is no dot in the name, pretend it exists after the
        // end of the string. The locale extensions will be tacked on there.

        if (dotx == -1) dotx = path.length();

        // TODO: Case where there is no suffix

        String baseName = path.substring(0, dotx);

        suffix = path.substring(dotx);

        baseNameLength = dotx;

        language = locale.getLanguage();
        country = locale.getCountry();
        variant = locale.getVariant();

        state = INITIAL;
        prevState = INITIAL;

        builder = new StringBuilder(baseName);

        advance();
    }

    private void advance()
    {
        prevState = state;

        while (state != EXHAUSTED)
        {
            state++;

            switch (state)
            {
                case LCV:

                    if (InternalUtils.isBlank(variant)) continue;

                    return;

                case LC:

                    if (InternalUtils.isBlank(country)) continue;

                    return;

                case LV:

                    // If country is null, then we've already generated this string
                    // as state LCV and we can continue directly to state L

                    if (InternalUtils.isBlank(variant) || InternalUtils.isBlank(country)) continue;

                    return;

                case L:

                    if (InternalUtils.isBlank(language)) continue;

                    return;

                case BARE:
                default:
                    return;
            }
        }
    }

    /**
     * Returns true if there are more name variants to be returned, false otherwise.
     */

    public boolean hasNext()
    {
        return state != EXHAUSTED;
    }

    /**
     * Returns the next localized variant.
     *
     * @throws NoSuchElementException if all variants have been returned.
     */

    public String next()
    {
        if (state == EXHAUSTED) throw new NoSuchElementException();

        String result = build();

        advance();

        return result;
    }

    private String build()
    {
        builder.setLength(baseNameLength);

        if (state == LC || state == LCV || state == L)
        {
            builder.append('_');
            builder.append(language);
        }

        // For LV, we want two underscores between language
        // and variant.

        if (state == LC || state == LCV || state == LV)
        {
            builder.append('_');

            if (state != LV) builder.append(country);
        }

        if (state == LV || state == LCV)
        {
            builder.append('_');
            builder.append(variant);
        }

        if (suffix != null) builder.append(suffix);

        return builder.toString();
    }

    public Locale getCurrentLocale()
    {
        switch (prevState)
        {
            case LCV:

                return new Locale(language, country, variant);

            case LC:

                return new Locale(language, country, "");

            case LV:

                return new Locale(language, "", variant);

            case L:

                return new Locale(language, "", "");

            default:
                return null;
        }
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * So that LNG may be used with the for loop.
     */
    public Iterator<String> iterator()
    {
        return this;
    }

}
