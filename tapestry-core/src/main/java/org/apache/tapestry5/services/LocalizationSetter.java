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

package org.apache.tapestry5.services;

/**
 * Sets the thread's locale given a desired locale. Note that the desired locale is just a hint. It wil try to honor it
 * but there is no guarantee that it will be used as is.
 * <p/>
 * Localization is controlled by the {@link org.apache.tapestry5.SymbolConstants#SUPPORTED_LOCALES} symbol.
 */
public interface LocalizationSetter
{
    /**
     * Determines if the provided potential locale name (presumably, extracted from a request URL) is a supported locale
     * name.  A call to this method will always set the {@link org.apache.tapestry5.ioc.services.ThreadLocale} (either
     * to the provided locale, if supported, or to the default locale).  If the locale name is supported, it will also
     * set the {@link org.apache.tapestry5.services.PersistentLocale} (which may affect how page and event links are
     * generated, to persist the selected locale across requests).
     * <p/>
     * Note that locale names <strong>are</strong> case sensitive.
     *
     * @param localeName name of locale to check (which may be blank or not a locale name)
     * @return true if the locale name is supported and the {@link org.apache.tapestry5.services.PersistentLocale} was
     *         set
     * @since 5.1.0.0
     */
    boolean setLocaleFromLocaleName(String localeName);

    /**
     * Allows the locale to be set from a specified locale name (which may be narrowed or defaulted to a support
     * locale). Does not set the persistent locale.
     *
     * @param localeName locale in effect for this request
     * @since 5.1.0.0.
     */
    void setNonPeristentLocaleFromLocaleName(String localeName);
}
