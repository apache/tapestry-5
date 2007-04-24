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

package org.apache.tapestry.ioc.internal.services;

import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.util.Locale;

import org.apache.tapestry.ioc.services.ThreadLocale;

public class ThreadLocaleImpl implements ThreadLocale
{
    private Locale _locale = Locale.getDefault();

    public Locale getLocale()
    {
        return _locale;
    }

    public void setLocale(Locale locale)
    {
        notNull(locale, "locale");

        _locale = locale;
    }
}
