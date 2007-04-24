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

package org.apache.tapestry.internal.services;

import java.util.Locale;

/**
 * Used as a key to lookup a page from the {@link org.apache.tapestry.internal.services.PagePool}.
 */
public class PageLocator
{
    private final String _pageName;

    private final Locale _locale;

    public PageLocator(String pageName, Locale locale)
    {
        _pageName = pageName;
        _locale = locale;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof PageLocator))
        {
            return false;
        }
        PageLocator locator = (PageLocator) obj;
        return _pageName.equals(locator._pageName) && _locale.equals(locator._locale);
    }

    @Override
    public int hashCode()
    {
        return _pageName.hashCode() * 17 + _locale.hashCode();
    }

    @Override
    public String toString()
    {
        return String.format("%s[%s, %s]", getClass().getSimpleName(), _pageName, _locale
                .toString());
    }
}
