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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.PersistentLocale;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Locale;

public class LocalizationSetterImplTest extends Assert
{
    private PersistentLocale nullPersistentLocale = new PersistentLocale()
    {
        public boolean isSet()
        {
            return false;
        }

        public Locale get()
        {
            return null;
        }

        public void set(Locale locale)
        {

        }

    };

    private PersistentLocale frenchPersistentLocale = new PersistentLocale()
    {
        public boolean isSet()
        {
            return true;
        }

        public Locale get()
        {
            return Locale.FRENCH;
        }

        public void set(Locale locale)
        {

        }

    };

    private static class ThreadLocaleImpl implements ThreadLocale
    {
        public Locale _locale;

        public void setLocale(Locale locale)
        {
            _locale = locale;
        }

        public Locale getLocale()
        {
            return _locale;
        }

    }

    @Test
    public void locale_split()
    {
        assertEquals(LocalizationSetterImpl.stripTerm("foo_bar_Baz"), "foo_bar");
        assertEquals(LocalizationSetterImpl.stripTerm("foo_bar"), "foo");
        assertEquals(LocalizationSetterImpl.stripTerm("foo"), "");
    }

    @Test
    public void to_locale_is_cached()
    {
        LocalizationSetterImpl filter = new LocalizationSetterImpl(nullPersistentLocale, null,
                                                                   "en");

        Locale l1 = filter.toLocale("en");

        assertEquals(l1.toString(), "en");

        checkLocale(l1, "en", "", "");

        assertSame(filter.toLocale("en"), l1);
    }

    private void checkLocale(Locale l, String expectedLanguage, String expectedCountry,
                             String expectedVariant)
    {
        assertEquals(l.getLanguage(), expectedLanguage);
        assertEquals(l.getCountry(), expectedCountry);
        assertEquals(l.getVariant(), expectedVariant);
    }

    @Test
    public void to_locale()
    {
        LocalizationSetterImpl filter = new LocalizationSetterImpl(nullPersistentLocale, null,
                                                                   "en");

        checkLocale(filter.toLocale("en"), "en", "", "");
        checkLocale(filter.toLocale("klingon_Gach"), "klingon", "GACH", "");
        checkLocale(filter.toLocale("klingon_Gach_snuff"), "klingon", "GACH", "snuff");
    }

    @Test
    public void known_locale()
    {
        ThreadLocale threadLocale = new ThreadLocaleImpl();
        threadLocale.setLocale(Locale.FRENCH);
        LocalizationSetter setter = new LocalizationSetterImpl(nullPersistentLocale, threadLocale,
                                                               "en,fr");
        setter.setThreadLocale(Locale.CANADA_FRENCH);
        assertEquals(threadLocale.getLocale(), Locale.FRENCH);

    }

    @Test
    public void unknown_locale_uses_default_locale()
    {
        ThreadLocale threadLocale = new ThreadLocaleImpl();
        threadLocale.setLocale(Locale.FRENCH);
        LocalizationSetter setter = new LocalizationSetterImpl(nullPersistentLocale, threadLocale,
                                                               "en,fr");
        setter.setThreadLocale(Locale.JAPANESE);
        assertEquals(threadLocale.getLocale(), Locale.ENGLISH);
    }

    @Test
    public void use_persistent_locale()
    {
        ThreadLocale threadLocale = new ThreadLocaleImpl();
        LocalizationSetter setter = new LocalizationSetterImpl(frenchPersistentLocale,
                                                               threadLocale, "en,fr");
        setter.setThreadLocale(Locale.ENGLISH);
        assertEquals(threadLocale.getLocale(), Locale.FRENCH);
    }

}
