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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.services.Cookies;

public class NoOpCookieSource implements Cookies
{

    public String readCookieValue(String name)
    {
        return null;
    }

    public void writeCookieValue(String name, String value)
    {

    }

    public void writeCookieValue(String name, String value, int maxAge)
    {

    }

    public void writeCookieValue(String name, String value, String path)
    {
        // TODO Auto-generated method stub

    }

    public void writeDomainCookieValue(String name, String value, String domain)
    {

    }

    public void writeDomainCookieValue(String name, String value, String domain, int maxAge)
    {

    }

    public void writeCookieValue(String name, String value, String path, String domain)
    {

    }

    public void removeCookieValue(String name)
    {

    }

}
