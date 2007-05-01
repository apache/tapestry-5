// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.util;

import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Used in a wide variety of resource searches. Generates a series of name variations from a path
 * (which must include a suffix) and locale.
 * <P>
 * This class is not threadsafe.
 */
public class LocalizedNameGenerator implements Iterator<String>, Iterable<String>
{
    private final int _baseNameLength;

    private final String _suffix;

    private final StringBuilder _builder;

    private final String _language;

    private final String _country;

    private final String _variant;

    private int _state;

    private int _prevState;

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

        // TODO: Case where there is no suffix

        String baseName = path.substring(0, dotx);

        _suffix = path.substring(dotx);

        _baseNameLength = dotx;

        _language = locale.getLanguage();
        _country = locale.getCountry();
        _variant = locale.getVariant();

        _state = INITIAL;
        _prevState = INITIAL;

        _builder = new StringBuilder(baseName);

        advance();
    }

    private void advance()
    {
        _prevState = _state;

        while (_state != EXHAUSTED)
        {
            _state++;

            switch (_state)
            {
                case LCV:

                    if (InternalUtils.isBlank(_variant)) continue;

                    return;

                case LC:

                    if (InternalUtils.isBlank(_country)) continue;

                    return;

                case LV:

                    // If _country is null, then we've already generated this string
                    // as state LCV and we can continue directly to state L

                    if (InternalUtils.isBlank(_variant) || InternalUtils.isBlank(_country))
                        continue;

                    return;

                case L:

                    if (InternalUtils.isBlank(_language)) continue;

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
        return _state != EXHAUSTED;
    }

    /**
     * Returns the next localized variant.
     * 
     * @throws NoSuchElementException
     *             if all variants have been returned.
     */

    public String next()
    {
        if (_state == EXHAUSTED) throw new NoSuchElementException();

        String result = build();

        advance();

        return result;
    }

    private String build()
    {
        _builder.setLength(_baseNameLength);

        if (_state == LC || _state == LCV || _state == L)
        {
            _builder.append('_');
            _builder.append(_language);
        }

        // For LV, we want two underscores between language
        // and variant.

        if (_state == LC || _state == LCV || _state == LV)
        {
            _builder.append('_');

            if (_state != LV) _builder.append(_country);
        }

        if (_state == LV || _state == LCV)
        {
            _builder.append('_');
            _builder.append(_variant);
        }

        if (_suffix != null) _builder.append(_suffix);

        return _builder.toString();
    }

    public Locale getCurrentLocale()
    {
        switch (_prevState)
        {
            case LCV:

                return new Locale(_language, _country, _variant);

            case LC:

                return new Locale(_language, _country, "");

            case LV:

                return new Locale(_language, "", _variant);

            case L:

                return new Locale(_language, "", "");

            default:
                return null;
        }
    }

    /** @throws UnsupportedOperationException */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /** So that LNG may be used with the for loop. */
    public Iterator<String> iterator()
    {
        return this;
    }

}
