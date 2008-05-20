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

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.Cookies;
import org.apache.tapestry5.services.PersistentLocale;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class PersistentLocaleImplTest extends Assert
{
    @Test
    public void get() throws IOException
    {
        Cookies cookieSource = new NoOpCookieSource()
        {

            @Override
            public String readCookieValue(String name)
            {
                return name.equals("org.apache.tapestry5.locale") ? "fr" : null;
            }

        };
        PersistentLocale persistentLocale = new PersistentLocaleImpl(cookieSource);
        assertEquals(persistentLocale.get(), Locale.FRENCH);
    }

    @Test
    public void get_none() throws IOException
    {
        Cookies cookieSource = new NoOpCookieSource()
        {

            @Override
            public String readCookieValue(String name)
            {
                return null;
            }

        };
        PersistentLocale persistentLocale = new PersistentLocaleImpl(cookieSource);
        assertNull(persistentLocale.get());
    }

    @Test
    public void set() throws IOException
    {
        final Map<String, String> cookies = CollectionFactory.newMap();
        Cookies cookieSource = new NoOpCookieSource()
        {
            @Override
            public void writeCookieValue(String name, String value)
            {
                cookies.put(name, value);
            }

        };
        PersistentLocale persistentLocale = new PersistentLocaleImpl(cookieSource);
        persistentLocale.set(Locale.CANADA_FRENCH);
        assertEquals(cookies.size(), 1);
        assertEquals(cookies.get("org.apache.tapestry5.locale"), "fr_CA");
    }

}
