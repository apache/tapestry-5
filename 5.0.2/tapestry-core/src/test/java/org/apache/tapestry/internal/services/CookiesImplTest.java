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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.tapestry.internal.services.ContextPathSource;
import org.apache.tapestry.internal.services.CookieSink;
import org.apache.tapestry.internal.services.CookieSource;
import org.apache.tapestry.internal.services.CookiesImpl;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.apache.tapestry.services.impl.CookiesImpl}.
 * 
 * @author Howard Lewis Ship
 */
@Test
public class CookiesImplTest extends Assert
{
    private static class ComparableCookie extends Cookie
    {
        public ComparableCookie(String name, String value, int maxAge)
        {
            super(name, value);
            setMaxAge(maxAge);
        }

        @Override
        public boolean equals(Object obj)
        {
            Cookie c = (Cookie) obj;

            return equals(getName(), c.getName()) && equals(getValue(), c.getValue())
                    && equals(getPath(), c.getPath()) && getMaxAge() == c.getMaxAge();
        }

        private boolean equals(Object value, Object other)
        {
            return value == other || (value != null && value.equals(other));
        }
    }

    private CookieSource newCookieSource(final String[] nameValues)
    {
        return new CookieSource()
        {
            public Cookie[] getCookies()
            {

                Cookie[] cookies = null;

                if (nameValues != null)
                {

                    List<Cookie> l = new ArrayList<Cookie>();

                    for (int i = 0; i < nameValues.length; i += 2)
                    {
                        String name = nameValues[i];
                        String value = nameValues[i + 1];

                        Cookie c = new Cookie(name, value);

                        l.add(c);
                    }

                    cookies = l.toArray(new Cookie[l.size()]);
                }
                return cookies;
            }
        };
    }

    private void attempt(String name, String expected, String[] nameValues)
    {
        // In seconds
        final int ONE_WEEK = 7 * 24 * 60 * 60;
        CookiesImpl cs = new CookiesImpl(null, newCookieSource(nameValues), null, ONE_WEEK);
        String actual = cs.readCookieValue(name);
        assertEquals(actual, expected);
    }

    public void test_No_Cookies()
    {
        attempt("foo", null, null);
    }

    public void test_Match()
    {
        attempt("fred", "flintstone", new String[]
        { "barney", "rubble", "fred", "flintstone" });
    }

    public void test_No_Match()
    {
        attempt("foo", null, new String[]
        { "bar", "baz" });
    }

    public void test_Write_Cookie_Domain()
    {
        final List<Cookie> cookies = CollectionFactory.newList();
        CookiesImpl cs = new CookiesImpl(new ContextPathSource()
        {

            public String getContextPath()
            {
                return "/context";
            }

        }, null, new CookieSink()
        {

            public void addCookie(Cookie cookie)
            {
                cookies.add(cookie);
            }

        }, 1000);

        cs.writeDomainCookieValue("foo", "bar", "fobar.com", 1234);
        Cookie expectedCookie = new ComparableCookie("foo", "bar", 1234);
        expectedCookie.setPath("/context/");
        expectedCookie.setDomain("fobar.com");
        assertEquals(cookies.size(), 1);
        assertEquals(cookies.get(0), expectedCookie);
    }

    public void test_Write_Cookie_With_Max_Age()
    {
        final List<Cookie> cookies = CollectionFactory.newList();
        CookiesImpl cs = new CookiesImpl(new ContextPathSource()
        {

            public String getContextPath()
            {
                return "/ctx";
            }

        }, null, new CookieSink()
        {

            public void addCookie(Cookie cookie)
            {
                cookies.add(cookie);
            }

        }, 1000);
        cs.writeCookieValue("foo", "bar", -1);
        Cookie expectedCookie = new ComparableCookie("foo", "bar", -1);
        expectedCookie.setPath("/ctx/");
        assertEquals(cookies.size(), 1);
        assertEquals(cookies.get(0), expectedCookie);
    }

    public void test_Write_Cookie()
    {
        final List<Cookie> cookies = CollectionFactory.newList();
        CookiesImpl cs = new CookiesImpl(new ContextPathSource()
        {

            public String getContextPath()
            {
                return "/ctx";
            }

        }, null, new CookieSink()
        {

            public void addCookie(Cookie cookie)
            {
                cookies.add(cookie);
            }

        }, 1000);
        cs.writeCookieValue("foo", "bar");
        Cookie expectedCookie = new ComparableCookie("foo", "bar", 1000);
        expectedCookie.setPath("/ctx/");
        assertEquals(cookies.size(), 1);
        assertEquals(cookies.get(0), expectedCookie);
    }

    public void test_Remove_Cookie()
    {
        final List<Cookie> cookies = CollectionFactory.newList();
        CookiesImpl cs = new CookiesImpl(new ContextPathSource()
        {

            public String getContextPath()
            {
                return "/ctx";
            }

        }, null, new CookieSink()
        {

            public void addCookie(Cookie cookie)
            {
                cookies.add(cookie);
            }

        }, 1000);
        cs.removeCookieValue("foo");
        Cookie expectedCookie = new ComparableCookie("foo", null, 0);
        expectedCookie.setPath("/ctx/");
        assertEquals(cookies.size(), 1);
        assertEquals(cookies.get(0), expectedCookie);
    }
}