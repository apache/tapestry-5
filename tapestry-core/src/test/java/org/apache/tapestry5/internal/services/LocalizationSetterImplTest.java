// Copyright 2006, 2009, 2010, 2012 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.services;

import static org.apache.tapestry5.commons.util.CollectionFactory.newSet;

import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.test.ioc.TestBase;
import org.testng.annotations.Test;

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
        LocalizationSetter setter = new LocalizationSetterImpl(null, null, null, "en");

        Locale l1 = setter.toLocale("en");

        assertEquals(l1.toString(), "en");

        checkLocale(l1, "en", "", "");

        assertSame(setter.toLocale("en"), l1);
    }

    private void checkLocale(Locale l, String expectedLanguage, String expectedCountry, String expectedVariant)
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

        LocalizationSetter setter = new LocalizationSetterImpl(request, pl, tl, "en,fr");

        assertTrue(setter.setLocaleFromLocaleName("fr"));

        verify();
    }

    @Test
    public void get_selected_locales()
    {
        LocalizationSetter setter = new LocalizationSetterImpl(null, null, null, "en,fr");

        assertListsEquals(setter.getSupportedLocales(), Locale.ENGLISH, Locale.FRENCH);
    }
    
    @Test
    public void get_selected_locale_names()
    {
        LocalizationSetter setter = new LocalizationSetterImpl(null, null, null, "en,fr");
        
        Object localeNames = TestBase.get(setter, "supportedLocaleNames");

        assertTrue(newSet("en", "fr").equals(localeNames));
    }
    
    @Test
    public void get_selected_locale_names_with_whitespaces()
    {
        LocalizationSetter setter = new LocalizationSetterImpl(null, null, null, "en, fr,  de");
        
        Object localeNames = TestBase.get(setter, "supportedLocaleNames");

        assertTrue(newSet("en", "fr", "de").equals(localeNames));
    }

    @Test
    public void get_locale_model()
    {
        LocalizationSetter setter = new LocalizationSetterImpl(null, null, null, "en,fr");

        SelectModel model = setter.getSupportedLocalesModel();

        assertNull(model.getOptionGroups());

        List<OptionModel> options = model.getOptions();

        assertEquals(options.size(), 2);

        assertEquals(options.get(0).getLabel(), "English");
        // Note that the label is localized to the underlying locale, not the default locale.
        // That's why its "français" (i.e., as a French speaker would say it), not "French"
        // (like an English speaker).
        assertEquals(options.get(1).getLabel(), "fran\u00e7ais");

        assertEquals(options.get(0).getValue(), Locale.ENGLISH);
        assertEquals(options.get(1).getValue(), Locale.FRENCH);

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

        setter.setNonPersistentLocaleFromLocaleName("fr_BE");

        verify();

    }
   
    @Test
    public void is_supported_locale_name()
    {
        PersistentLocale pl = mockPersistentLocale();
        ThreadLocale tl = mockThreadLocale();
        Request request = mockRequest();


        replay();

        LocalizationSetterImpl setter = new LocalizationSetterImpl(request, pl, tl, "de, de_DE, de_CH,en");

        assertTrue(setter.isSupportedLocaleName("de"));
        assertTrue(setter.isSupportedLocaleName("de_de"));
        assertTrue(setter.isSupportedLocaleName("de_de"));
        assertTrue(setter.isSupportedLocaleName("de_DE"));
        assertTrue(setter.isSupportedLocaleName("de_ch"));
        assertTrue(setter.isSupportedLocaleName("de_CH"));
        assertTrue(setter.isSupportedLocaleName("en"));

        verify();

    }
}
