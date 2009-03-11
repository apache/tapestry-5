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

package org.apache.tapestry5.services;

import java.util.Locale;

/**
 * Manages the persistent locale stored in the browser (inside the URL).
 *
 * @see LocalizationSetter#setLocaleFromLocaleName(String)
 * @see org.apache.tapestry5.services.LocalizationSetter
 * @see org.apache.tapestry5.services.ComponentEventLinkEncoder
 */
public interface PersistentLocale
{
    /**
     * Sets the locale value that will be encoded into the response. This must match a locale configured via {@link
     * org.apache.tapestry5.SymbolConstants#SUPPORTED_LOCALES}.
     *
     * @throws IllegalArgumentException if the locale is not valid
     */
    void set(Locale locale);

    /**
     * Gets the locale obtained from the request, or null if the response did not indicate a specific locale (in which
     * case the active locale may have been determined from request headers).
     */
    Locale get();

    /**
     * @return true if a locale was present in the request URL; false otherwise.
     */
    boolean isSet();
}
