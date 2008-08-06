// Copyright 2006, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

import java.util.Locale;

public class LocalizedNameGeneratorTest extends IOCTestCase
{

    private void run(String path, Locale locale, String... expected)
    {
        LocalizedNameGenerator g = new LocalizedNameGenerator(path, locale);

        for (String s : expected)
        {
            assertTrue(g.hasNext());
            assertEquals(g.next(), s);
        }

        assertFalse(g.hasNext());
    }

    @Test
    public void locale_with_language_and_country()
    {
        run("basic.test", Locale.US, "basic_en_US.test", "basic_en.test", "basic.test");
    }

    @Test
    public void locale_with_just_language()
    {
        run("noCountry.zap", Locale.FRENCH, "noCountry_fr.zap", "noCountry.zap");
    }

    @Test
    public void locale_with_variant_but_no_country()
    {

        // The double-underscore is correct, it's a kind
        // of placeholder for the null country.
        // JDK1.3 always converts the locale to upper case. JDK 1.4
        // does not. To keep this test happyt, we selected an all-uppercase
        // locale.

        run("fred.foo", new Locale("en", "", "GEEK"), "fred_en__GEEK.foo", "fred_en.foo", "fred.foo");
    }

    @Test
    public void locale_with_just_language_no_period()
    {
        run("context:/blah", Locale.FRENCH, "context:/blah_fr", "context:/blah");
    }

    @Test
    public void locale_with_variant_but_no_country_no_period()
    {
        run("context:/blah", new Locale("fr", "", "GEEK"), "context:/blah_fr__GEEK", "context:/blah_fr",
            "context:/blah");

    }
}
