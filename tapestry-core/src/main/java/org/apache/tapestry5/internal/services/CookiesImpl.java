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

import org.apache.tapestry5.ioc.annotations.IntermediateType;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.util.TimeInterval;
import org.apache.tapestry5.services.Cookies;
import org.apache.tapestry5.services.Request;

import javax.servlet.http.Cookie;

/**
 * Implementation of the {@link org.apache.tapestry5.services.Cookies} service interface.
 */
public class CookiesImpl implements Cookies
{
    private final Request request;

    private final CookieSource cookieSource;

    private final CookieSink cookieSink;

    private final int defaultMaxAge;

    /**
     * @param request
     * @param cookieSource
     * @param cookieSink
     * @param defaultMaxAge default cookie expiration time in milliseconds
     */
    public CookiesImpl(Request request,

                       CookieSource cookieSource,

                       CookieSink cookieSink,

                       @Symbol("tapestry.default-cookie-max-age") @IntermediateType(TimeInterval.class)
                       long defaultMaxAge)
    {
        this.request = request;
        this.cookieSource = cookieSource;
        this.cookieSink = cookieSink;
        this.defaultMaxAge = (int) (defaultMaxAge / 1000l);
    }

    public String readCookieValue(String name)
    {
        Cookie[] cookies = cookieSource.getCookies();

        if (cookies == null) return null;

        for (Cookie cooky : cookies)
        {
            if (cooky.getName().equals(name)) return cooky.getValue();
        }

        return null;
    }

    public void writeCookieValue(String name, String value)
    {
        writeCookieValue(name, value, defaultMaxAge);
    }

    public void writeCookieValue(String name, String value, int maxAge)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(request.getContextPath() + "/");
        cookie.setMaxAge(maxAge);
        cookie.setSecure(request.isSecure());

        cookieSink.addCookie(cookie);
    }

    public void writeCookieValue(String name, String value, String path)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setMaxAge(defaultMaxAge);
        cookie.setSecure(request.isSecure());

        cookieSink.addCookie(cookie);
    }

    public void writeDomainCookieValue(String name, String value, String domain)
    {
        writeDomainCookieValue(name, value, domain, defaultMaxAge);
    }

    public void writeDomainCookieValue(String name, String value, String domain, int maxAge)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(request.getContextPath() + "/");
        cookie.setDomain(domain);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(request.isSecure());

        cookieSink.addCookie(cookie);
    }

    public void writeCookieValue(String name, String value, String path, String domain)
    {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setDomain(domain);
        cookie.setMaxAge(defaultMaxAge);
        cookie.setSecure(request.isSecure());

        cookieSink.addCookie(cookie);
    }

    public void removeCookieValue(String name)
    {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath(request.getContextPath() + "/");
        cookie.setMaxAge(0);
        cookie.setSecure(request.isSecure());

        cookieSink.addCookie(cookie);
    }
}
