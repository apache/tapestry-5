// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.services.PersistentLocale;

import java.util.Locale;
import java.util.Set;

public class PersistentLocaleImpl implements PersistentLocale
{
    private final PerthreadManager perThreadManager;

    private final String supportedLocales;

    private final Set<String> localeNames = CollectionFactory.newSet();

    public PersistentLocaleImpl(PerthreadManager perThreadManager,

                                @Inject @Symbol(SymbolConstants.SUPPORTED_LOCALES)
                                String supportedLocales)
    {
        this.perThreadManager = perThreadManager;
        this.supportedLocales = supportedLocales;

        for (String name : TapestryInternalUtils.splitAtCommas(supportedLocales))
        {
            localeNames.add(name.toLowerCase());
        }
    }

    public void set(Locale locale)
    {
        Defense.notNull(locale, "locale");

        if (!localeNames.contains(locale.toString().toLowerCase()))
        {
            String message = String.format(
                    "Locale '%s' is not supported by this application. Supported locales are '%s'; this is configured via the %s symbol.",
                    locale, supportedLocales, SymbolConstants.SUPPORTED_LOCALES);

            throw new IllegalArgumentException(message);
        }


        perThreadManager.put(this, locale);
    }

    public Locale get()
    {
        return (Locale) perThreadManager.get(this);
    }

    public boolean isSet()
    {
        return get() != null;
    }
}
