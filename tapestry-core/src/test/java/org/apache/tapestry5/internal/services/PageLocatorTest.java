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

package org.apache.tapestry5.internal.services;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Locale;

public class PageLocatorTest extends Assert
{
    @Test
    public void equals()
    {
        PageLocator locator = new PageLocator("p1", Locale.ENGLISH);
        assertEquals(locator, new PageLocator("p1", Locale.ENGLISH));
        assertFalse(locator.equals(null));
        assertFalse(locator.equals("p1"));
        assertFalse(locator.equals(new PageLocator("p1", Locale.CHINESE)));
        assertFalse(locator.equals(new PageLocator("p2", Locale.ENGLISH)));
    }

    @Test
    public void to_string()
    {
        PageLocator locator = new PageLocator("p1", Locale.ENGLISH);
        assertEquals(locator.toString(), "PageLocator[p1, en]");
    }

}
