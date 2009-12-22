// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.internal.util.LocaleUtils;
import org.apache.tapestry5.services.Cookies;
import org.apache.tapestry5.services.PersistentLocale;

import java.util.Locale;

public class PersistentLocaleImpl implements PersistentLocale
{
    /**
     * Name of the cookie written to the client web browser to identify the locale.
     */
    private static final String LOCALE_COOKIE_NAME = "org.apache.tapestry5.locale";

    private final Cookies cookieSource;

    public PersistentLocaleImpl(Cookies cookieSource)
    {
        this.cookieSource = cookieSource;
    }

    public void set(Locale locale)
    {
        cookieSource.writeCookieValue(LOCALE_COOKIE_NAME, locale.toString());
    }

    public Locale get()
    {
        String localeCookieValue = getCookieValue();

        return localeCookieValue != null ? LocaleUtils.toLocale(localeCookieValue) : null;
    }

    private String getCookieValue()
    {
        return cookieSource.readCookieValue(LOCALE_COOKIE_NAME);
    }

    public boolean isSet()
    {
        return getCookieValue() != null;
    }

}
