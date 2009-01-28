// Copyright 2006, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.services.Request;
import org.testng.annotations.Test;

import java.util.Locale;

public class LocalizationSetterImplTest extends InternalBaseTestCase
{

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
        LocalizationSetterImpl setter = new LocalizationSetterImpl(null, null, null, "en");

        Locale l1 = setter.toLocale("en");

        assertEquals(l1.toString(), "en");

        checkLocale(l1, "en", "", "");

        assertSame(setter.toLocale("en"), l1);
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
        LocalizationSetterImpl setter = new LocalizationSetterImpl(null, null, null, "en");

        checkLocale(setter.toLocale("en"), "en", "", "");
        checkLocale(setter.toLocale("klingon_Gach"), "klingon", "GACH", "");
        checkLocale(setter.toLocale("klingon_Gach_snuff"), "klingon", "GACH", "snuff");
    }

    @Test
    public void known_locale()
    {
        PersistentLocale pl = mockPersistentLocale();
        ThreadLocale tl = mockThreadLocale();
        Request request = mockRequest();

        tl.setLocale(Locale.FRENCH);
        pl.set(Locale.FRENCH);

        replay();

        LocalizationSetterImpl setter = new LocalizationSetterImpl(request, pl, tl, "en,fr");

        assertTrue(setter.setLocaleFromLocaleName("fr"));

        verify();
    }

    protected final PersistentLocale mockPersistentLocale()
    {
        return newMock(PersistentLocale.class);
    }

    @Test
    public void unknown_locale_uses_locale_from_request()
    {
        PersistentLocale pl = mockPersistentLocale();
        ThreadLocale tl = mockThreadLocale();
        Request request = mockRequest();

        tl.setLocale(Locale.FRENCH);

        train_getLocale(request, Locale.CANADA_FRENCH);

        replay();

        LocalizationSetterImpl setter = new LocalizationSetterImpl(request, pl, tl, "en,fr");

        assertFalse(setter.setLocaleFromLocaleName("unknown"));

        verify();
    }

    @Test
    public void unsupported_locale_in_request_uses_default_locale()
    {
        PersistentLocale pl = mockPersistentLocale();
        ThreadLocale tl = mockThreadLocale();
        Request request = mockRequest();

        tl.setLocale(Locale.ITALIAN);

        train_getLocale(request, Locale.CHINESE);

        replay();

        LocalizationSetterImpl setter = new LocalizationSetterImpl(request, pl, tl, "it,en,fr");

        assertFalse(setter.setLocaleFromLocaleName("unknown"));

        verify();
    }

    @Test
    public void set_nonpersistent_locale()
    {
        PersistentLocale pl = mockPersistentLocale();
        ThreadLocale tl = mockThreadLocale();
        Request request = mockRequest();

        tl.setLocale(Locale.FRENCH);

        replay();

        LocalizationSetterImpl setter = new LocalizationSetterImpl(request, pl, tl, "en,fr");

        setter.setNonPeristentLocaleFromLocaleName("fr_BE");

        verify();

    }
}
