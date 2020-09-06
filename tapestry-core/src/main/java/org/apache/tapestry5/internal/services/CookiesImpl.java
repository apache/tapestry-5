// Copyright 2007, 2008, 2012 The Apache Software Foundation
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

import javax.servlet.http.Cookie;

import org.apache.tapestry5.CookieBuilder;
import org.apache.tapestry5.commons.util.TimeInterval;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.IntermediateType;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Cookies;

/**
 * Implementation of the {@link org.apache.tapestry5.services.Cookies} service interface.
 */
public class CookiesImpl implements Cookies
{
    private final Request request;

    private final CookieSource cookieSource;

    private final CookieSink cookieSink;

    private final String defaultCookiePath;

    private final int defaultMaxAge;

    /**
     * @param request
     * @param cookieSource
     * @param cookieSink
     * @param contextPath
     * @param defaultMaxAge
     *         default cookie expiration time in milliseconds
     */
    public CookiesImpl(Request request,

                       CookieSource cookieSource,

                       CookieSink cookieSink,

                       @Symbol(TapestryHttpSymbolConstants.CONTEXT_PATH)
                       String contextPath,

                       @Symbol("tapestry.default-cookie-max-age") @IntermediateType(TimeInterval.class)
                       long defaultMaxAge)
    {
        this.request = request;
        this.cookieSource = cookieSource;
        this.cookieSink = cookieSink;
        this.defaultCookiePath = contextPath + "/";
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
        getBuilder(name, value).write();
    }

    public void writeCookieValue(String name, String value, int maxAge)
    {
        getBuilder(name, value).setMaxAge(maxAge).write();
    }

    public void writeCookieValue(String name, String value, String path)
    {
        getBuilder(name, value).setPath(path).write();
    }

    public void writeDomainCookieValue(String name, String value, String domain)
    {
        getBuilder(name, value).setDomain(domain).write();
    }

    public void writeDomainCookieValue(String name, String value, String domain, int maxAge)
    {
        getBuilder(name, value).setDomain(domain).setMaxAge(maxAge).write();
    }

    public void writeCookieValue(String name, String value, String path, String domain)
    {
        getBuilder(name, value).setPath(path).setDomain(domain).write();
    }

    public void removeCookieValue(String name)
    {
        getBuilder(name, null).delete();
    }

    public CookieBuilder getBuilder(String name, String value)
    {
        CookieBuilder builder = new CookieBuilder(name, value)
        {
            @Override
            public void write()
            {
                Cookie cookie = new Cookie(name, value);

                cookie.setPath(path == null ? defaultCookiePath : path);

                if(domain != null)
                    cookie.setDomain(domain);

                cookie.setMaxAge(maxAge == null ? defaultMaxAge : maxAge);

                cookie.setSecure(secure == null ? request.isSecure() : secure);

                cookie.setVersion(version);

                if (comment != null)
                {
                    cookie.setComment(comment);
                }

                if (httpOnly != null)
                {
                    cookie.setHttpOnly(httpOnly);
                }

                cookieSink.addCookie(cookie);
            }

            @Override
            public void delete()
            {
                setMaxAge(0);

                write();
            }
        };

        return builder;
    }
}
