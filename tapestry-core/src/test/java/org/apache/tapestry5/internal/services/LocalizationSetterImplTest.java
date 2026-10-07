// Copyright 2006, 2009, 2010, 2012, 2026 The Apache Software Foundation
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
import java.util.Map;

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

    @SuppressWarnings("unchecked")
    private static Map<String, Locale> localeCache(LocalizationSetterImpl setter)
    {
        return (Map<String, Locale>) TestBase.get(setter, "localeCache");
    }

    @Test
    public void is_cacheable_locale_name()
    {
        // well-formed names are cacheable
        assertTrue(LocalizationSetterImpl.isCacheableLocaleName("en"));
        assertTrue(LocalizationSetterImpl.isCacheableLocaleName("en_US"));
        assertTrue(LocalizationSetterImpl.isCacheableLocaleName("en_US_POSIX"));
        assertTrue(LocalizationSetterImpl.isCacheableLocaleName("es_419"));       // numeric UN M.49 region
        assertTrue(LocalizationSetterImpl.isCacheableLocaleName("klingon_Gach_snuff"));

        // malformed / hostile names are not
        assertFalse(LocalizationSetterImpl.isCacheableLocaleName(""));
        assertFalse(LocalizationSetterImpl.isCacheableLocaleName("en_US_en_US"));  // more than three terms
        assertFalse(LocalizationSetterImpl.isCacheableLocaleName("en-US"));        // non-alphanumeric separator
        assertFalse(LocalizationSetterImpl.isCacheableLocaleName("en/../secret")); // path-ish junk

        // a single over-long term, bounded by MAX_TERM_LENGTH...
        assertFalse(LocalizationSetterImpl.isCacheableLocaleName(
                repeat('a', LocalizationSetterImpl.MAX_TERM_LENGTH + 1)));

        assertFalse(LocalizationSetterImpl.isCacheableLocaleName(
                "en_US_" + repeat('a', LocalizationSetterImpl.MAX_TERM_LENGTH + 1)));

        // a genuinely huge string is rejected without being fully scanned
        assertFalse(LocalizationSetterImpl.isCacheableLocaleName(repeat('a', 8192)));
    }

    @Test
    public void supported_locales_are_seeded_into_the_cache()
    {
        LocalizationSetterImpl setter = new LocalizationSetterImpl(null, null, null, "en,fr");

        Map<String, Locale> cache = localeCache(setter);

        // pre-seeded at construction, before any request can be processed
        assertTrue(cache.containsKey("en"));
        assertTrue(cache.containsKey("fr"));
    }

    @Test
    public void locale_cache_is_size_bounded()
    {
        LocalizationSetterImpl setter = new LocalizationSetterImpl(null, null, null, "en");

        // Fill the cache with distinct, well-formed short names.
        // The cache must stop growing at its cap rather than expanding without bound.
        for (int i = 0; i < LocalizationSetterImpl.MAX_LOCALE_CACHE_SIZE + 500; i++)
        {
            setter.toLocale("zz" + i);
        }

        assertTrue(localeCache(setter).size() <= LocalizationSetterImpl.MAX_LOCALE_CACHE_SIZE);
    }

    @Test
    public void overly_long_locale_names_are_not_cached()
    {
        LocalizationSetterImpl setter = new LocalizationSetterImpl(null, null, null, "en");

        // A well-formed short name is still cached (same instance returned on the second call)...
        assertSame(setter.toLocale("de"), setter.toLocale("de"));

        // ...large name is resolved without being retained, so it is re-parsed and
        // a fresh (non-cached) instance comes back each time.
        String longName = repeat('a', 8192);

        assertNotNull(setter.toLocale(longName));
        assertNotSame(setter.toLocale(longName), setter.toLocale(longName));
        assertFalse(localeCache(setter).containsKey(longName));
    }

    @Test
    public void attacker_supplied_locale_name_is_not_retained()
    {
        PersistentLocale pl = mockPersistentLocale();
        ThreadLocale tl = mockThreadLocale();
        Request request = mockRequest();

        // an unresolvable, over-long name narrows to the default (first supported) locale
        tl.setLocale(Locale.ENGLISH);

        replay();

        LocalizationSetterImpl setter = new LocalizationSetterImpl(request, pl, tl, "en,fr");

        String attackerName = repeat('x', 8192);

        setter.setNonPersistentLocaleFromLocaleName(attackerName);

        verify();

        assertFalse(localeCache(setter).containsKey(attackerName));
    }

    private static String repeat(char c, int count)
    {
        StringBuilder builder = new StringBuilder(count);

        for (int i = 0; i < count; i++)
        {
            builder.append(c);
        }

        return builder.toString();
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
    public void set_nonpersistent_locale_from_request()
    {
        PersistentLocale pl = mockPersistentLocale();
        ThreadLocale tl = mockThreadLocale();
        Request request = mockRequest();

        tl.setLocale(Locale.FRENCH);

        train_getLocale(request, Locale.CANADA_FRENCH);

        replay();

        LocalizationSetterImpl setter = new LocalizationSetterImpl(request, pl, tl, "en,fr");

        setter.setNonPersistentLocaleFromRequest(request);

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
