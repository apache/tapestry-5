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

package org.apache.tapestry.internal.services;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ComponentInvocationTest extends Assert
{
    @Test
    public void no_context()
    {
        ComponentInvocation invocation = new ComponentInvocation(new OpaqueConstantTarget("abc"),
                                                                 new String[0], null);
        assertEquals(invocation.buildURI(false), "abc");
        assertEquals(invocation.buildURI(true), "abc");
    }

    @Test
    public void context()
    {
        ComponentInvocation invocation = new ComponentInvocation(new OpaqueConstantTarget("abc"),
                                                                 new String[]
                                                                         {"x", "123"}, null);
        assertEquals(invocation.buildURI(false), "abc/x/123");
        assertEquals(invocation.buildURI(true), "abc/x/123");
    }

    @Test
    public void parameters()
    {
        ComponentInvocation invocation = new ComponentInvocation(new OpaqueConstantTarget("abc"),
                                                                 new String[]
                                                                         {"x", "123"}, null);
        invocation.addParameter("p1", "foo");
        invocation.addParameter("p2", "bar");
        assertEquals(invocation.buildURI(false), "abc/x/123?p1=foo&p2=bar");
        assertEquals(invocation.buildURI(true), "abc/x/123");
    }
}
