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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ioc.annotations.Symbol;
import org.apache.tapestry.services.Cookies;
import org.apache.tapestry.services.Request;

import javax.servlet.http.Cookie;

/**
 * Implementation of the {@link org.apache.tapestry.services.Cookies} service interface.
 */
public class CookiesImpl implements Cookies
{
    private Request _request;

    private CookieSource _cookieSource;

    private CookieSink _cookieSink;

    private int _defaultMaxAge;

    public CookiesImpl(Request request,

                       CookieSource cookieSource,

                       CookieSink cookieSink,

                       @Symbol("tapestry.default-cookie-max-age")
                       int defaultMaxAge)
    {
        _request = request;
        _cookieSource = cookieSource;
        _cookieSink = cookieSink;
        _defaultMaxAge = defaultMaxAge;
    }

    public String readCookieValue(String name)
    {
        Cookie[] cookies = _cookieSource.getCookies();

        if (cookies == null) return null;

        for (int i = 0; i < cookies.length; i++)
        {
            if (cookies[i].getName().equals(name)) return cookies[i].getValue();
        }

        return null;
    }

    public void writeCookieValue(String name, String value)
    {
        writeCookieValue(name, value, _defaultMaxAge);
    }

    public void writeCookieValue(String name, String value, int maxAge)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(_request.getContextPath() + "/");
        cookie.setMaxAge(maxAge);

        _cookieSink.addCookie(cookie);
    }

    public void writeCookieValue(String name, String value, String path)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);

        _cookieSink.addCookie(cookie);
    }

    public void writeDomainCookieValue(String name, String value, String domain)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(_request.getContextPath() + "/");
        cookie.setDomain(domain);

        _cookieSink.addCookie(cookie);
    }

    public void writeDomainCookieValue(String name, String value, String domain, int maxAge)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(_request.getContextPath() + "/");
        cookie.setDomain(domain);
        cookie.setMaxAge(maxAge);

        _cookieSink.addCookie(cookie);
    }

    public void writeCookieValue(String name, String value, String path, String domain)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setDomain(domain);

        _cookieSink.addCookie(cookie);
    }

    public void removeCookieValue(String name)
    {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath(_request.getContextPath() + "/");
        cookie.setMaxAge(0);

        _cookieSink.addCookie(cookie);
    }

}
