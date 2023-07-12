// Copyright 2023 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.services;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.apache.tapestry5.services.pageload.PageCachingReferenceTypeService;
import org.apache.tapestry5.services.pageload.ReferenceType;
import org.testng.annotations.Test;

public class ReferenceTypeTest
{
    private static final String PAGE1 = "lib1/Something";
    private static final String PAGE2 = "lib2/Else";
    private static final String ANOTHER_PAGE = "another";
    
    @Test
    public void forPages()
    {
        
        final PageCachingReferenceTypeService instance = ReferenceType.STRONG.forPages(PAGE1, PAGE2);
        
        assertNull(instance.get(ANOTHER_PAGE));
        
        assertEquals(instance.get(PAGE1), ReferenceType.STRONG);
        assertEquals(instance.get(PAGE2), ReferenceType.STRONG);
        assertEquals(instance.get(PAGE1.toLowerCase()), ReferenceType.STRONG);
        assertEquals(instance.get(PAGE2.toLowerCase()), ReferenceType.STRONG);
        assertEquals(instance.get(PAGE1.toUpperCase()), ReferenceType.STRONG);
        assertEquals(instance.get(PAGE2.toUpperCase()), ReferenceType.STRONG);

        assertNull(instance.get(PAGE1 + "a"));
        assertNull(instance.get(PAGE2 + "b"));
        
    }
    
}
