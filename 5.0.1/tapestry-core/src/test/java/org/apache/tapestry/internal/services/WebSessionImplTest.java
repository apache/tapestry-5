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

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.Session;
import org.testng.annotations.Test;

public class WebSessionImplTest extends InternalBaseTestCase
{
    @Test
    public void get_attribute_names()
    {
        Enumeration e = Collections.enumeration(Arrays.asList("fred", "barney"));
        HttpSession hs = newHttpSession();

        expect(hs.getAttributeNames()).andReturn(e);

        replay();

        Session session = new SessionImpl(hs);

        assertEquals(session.getAttributeNames(), Arrays.asList("barney", "fred"));

        verify();
    }

    @Test
    public void get_attribute_names_by_prefix()
    {
        Enumeration e = Collections.enumeration(Arrays.asList("fred", "barney", "fanny"));
        HttpSession hs = newHttpSession();

        expect(hs.getAttributeNames()).andReturn(e);

        replay();

        Session session = new SessionImpl(hs);

        assertEquals(session.getAttributeNames("f"), Arrays.asList("fanny", "fred"));

        verify();
    }
}
