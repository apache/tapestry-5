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

package org.apache.tapestry.internal.test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

public class PageTesterSessionTest extends Assert
{
    private PageTesterSession _session;

    @BeforeMethod
    public void before()
    {
        _session = new PageTesterSession();
    }

    @Test
    public void empty()
    {
        assertEquals(_session.getAttributeNames(), Collections.EMPTY_LIST);
        assertNull(_session.getAttribute("x"));
    }

    @Test
    public void set_attributes()
    {
        _session.setAttribute("b", 10);
        _session.setAttribute("a", 20);
        assertEquals(_session.getAttribute("a"), 20);
        assertEquals(_session.getAttribute("b"), 10);
    }

    @Test
    public void remove_if_value_is_null()
    {
        _session.setAttribute("b", 10);
        _session.setAttribute("a", 20);
        assertEquals(_session.getAttributeNames().size(), 2);
        _session.setAttribute("b", null);
        assertEquals(_session.getAttributeNames().size(), 1);
    }

    @Test
    public void names_sorted()
    {
        _session.setAttribute("b", 10);
        _session.setAttribute("a", 20);
        _session.setAttribute("c", 50);
        assertEquals(_session.getAttributeNames(), Arrays.asList("a", "b", "c"));
    }
}
