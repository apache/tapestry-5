// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal.test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

public class PageTesterSessionTest extends Assert
{
    private PageTesterSession session;

    @BeforeMethod
    public void before()
    {
        session = new PageTesterSession();
    }

    @Test
    public void empty()
    {
        assertEquals(session.getAttributeNames(), Collections.EMPTY_LIST);
        assertNull(session.getAttribute("x"));
    }

    @Test
    public void set_attributes()
    {
        session.setAttribute("b", 10);
        session.setAttribute("a", 20);
        assertEquals(session.getAttribute("a"), 20);
        assertEquals(session.getAttribute("b"), 10);
    }

    @Test
    public void remove_if_value_is_null()
    {
        session.setAttribute("b", 10);
        session.setAttribute("a", 20);
        assertEquals(session.getAttributeNames().size(), 2);
        session.setAttribute("b", null);
        assertEquals(session.getAttributeNames().size(), 1);
    }

    @Test
    public void names_sorted()
    {
        session.setAttribute("b", 10);
        session.setAttribute("a", 20);
        session.setAttribute("c", 50);
        assertEquals(session.getAttributeNames(), Arrays.asList("a", "b", "c"));
    }
}
