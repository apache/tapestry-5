// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.test;

import org.apache.tapestry.internal.services.CookieSink;
import org.apache.tapestry.internal.services.CookieSource;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;

import javax.servlet.http.Cookie;
import java.util.Map;

public class TestableCookieSinkSource implements CookieSource, CookieSink
{
    private Map<String, Cookie> _cookies;

    public TestableCookieSinkSource()
    {
        _cookies = CollectionFactory.newMap();
    }

    public Cookie[] getCookies()
    {
        return _cookies.values().toArray(new Cookie[_cookies.size()]);
    }

    public void addCookie(Cookie cookie)
    {
        _cookies.put(cookie.getName(), cookie);
    }

}
