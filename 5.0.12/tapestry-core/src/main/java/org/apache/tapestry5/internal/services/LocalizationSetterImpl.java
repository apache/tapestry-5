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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.SymbolConstants;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Given a set of supported locales, for a specified desired locale, sets the current thread's locale to a supported
 * locale that is closest to the desired.
 */
public class LocalizationSetterImpl implements LocalizationSetter
{
    private final ThreadLocale threadLocale;

    private final Locale defaultLocale;

    private final Set<String> acceptedLocaleNames;

    private final Map<String, Locale> localeCache = CollectionFactory.newConcurrentMap();

    private final PersistentLocale persistentLocale;

    public LocalizationSetterImpl(PersistentLocale persistentLocale, ThreadLocale threadLocale,
                                  @Inject
                                  @Symbol(SymbolConstants.SUPPORTED_LOCALES)
                                  String acceptedLocaleNames)
    {
        this.persistentLocale = persistentLocale;

        this.threadLocale = threadLocale;

        String[] names = acceptedLocaleNames.split(",");

        defaultLocale = toLocale(names[0]);

        this.acceptedLocaleNames = CollectionFactory.newSet(names);
    }

    Locale toLocale(String localeName)
    {
        Locale result = localeCache.get(localeName);

        if (result == null)
        {
            result = constructLocale(localeName);
            localeCache.put(localeName, result);
        }

        return result;
    }

    private Locale constructLocale(String name)
    {
        String[] terms = name.split("_");

        switch (terms.length)
        {
            case 1:
                return new Locale(terms[0], "");

            case 2:
                return new Locale(terms[0], terms[1]);

            case 3:

                return new Locale(terms[0], terms[1], terms[2]);

            default:

                throw new IllegalArgumentException();
        }
    }

    public void setThreadLocale(Locale desiredLocale)
    {
        if (persistentLocale.get() != null) desiredLocale = persistentLocale.get();

        Locale locale = findClosestAcceptedLocale(desiredLocale);

        threadLocale.setLocale(locale);
    }

    private Locale findClosestAcceptedLocale(Locale desiredLocale)
    {
        String localeName = desiredLocale.toString();

        while (true)
        {
            if (acceptedLocaleNames.contains(localeName)) return toLocale(localeName);

            localeName = stripTerm(localeName);

            if (localeName.length() == 0) break;
        }

        return defaultLocale;
    }

    static String stripTerm(String localeName)
    {
        int scorex = localeName.lastIndexOf('_');

        return scorex < 0 ? "" : localeName.substring(0, scorex);
    }

}
