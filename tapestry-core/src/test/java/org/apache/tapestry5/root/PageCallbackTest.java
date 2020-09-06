// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.root;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.PageCallback;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.internal.services.ArrayEventContext;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PageCallbackTest extends InternalBaseTestCase
{
    private TypeCoercer typeCoercer;

    @BeforeClass
    public void setup()
    {
        typeCoercer = getService(TypeCoercer.class);
    }

    @Test
    public void callback_with_no_context()
    {
        PageRenderLinkSource source = mockPageRenderLinkSource();
        Link link = mockLink();

        expect(source.createPageRenderLinkWithContext("foo")).andReturn(link);

        PageCallback pc = new PageCallback("foo");

        assertEquals(pc.toString(), "PageCallback[foo]");

        replay();

        assertSame(pc.toLink(source), link);

        verify();
    }

    @Test
    public void callback_with_context()
    {
        EventContext context = new ArrayEventContext(typeCoercer, 1, 2);

        PageRenderLinkSource source = mockPageRenderLinkSource();
        Link link = mockLink();

        expect(source.createPageRenderLinkWithContext("bar", "1", "2")).andReturn(link);

        PageCallback pc = new PageCallback("bar", context);

        assertEquals(pc.toString(), "PageCallback[bar 1/2]");

        replay();

        assertSame(pc.toLink(source), link);

        verify();
    }
}
