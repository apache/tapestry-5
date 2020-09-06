// Copyright 2009, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.integration.app3.pages.Index;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.testng.annotations.Test;

public class PageRenderLinkSourceImplTest extends InternalBaseTestCase
{
    private static final Class PAGE_CLASS = Index.class;

    private static final String PAGE_NAME = "Index";

    @Test
    public void default_passivate_context()
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        LinkSource source = mockLinkSource();
        Link link = mockLink();

        train_resolvePageClassNameToPageName(resolver, PAGE_CLASS.getName(), PAGE_NAME);

        expect(source.createPageRenderLink(PAGE_NAME, false)).andReturn(link);

        replay();

        PageRenderLinkSource service = new PageRenderLinkSourceImpl(source, resolver);

        assertSame(service.createPageRenderLink(PAGE_CLASS), link);

        verify();
    }

    @Test
    public void override_passivate_context()
    {
        ComponentClassResolver resolver = mockComponentClassResolver();
        LinkSource source = mockLinkSource();
        Link link = mockLink();
        EventContext eventContext = mockEventContext();

        train_resolvePageClassNameToPageName(resolver, PAGE_CLASS.getName(), PAGE_NAME);

        expect(source.createPageRenderLink(PAGE_NAME, true, "fred", "barney")).andReturn(link);

        train_resolvePageClassNameToPageName(resolver, PAGE_CLASS.getName(), PAGE_NAME);

        train_getCount(eventContext, 2);        

        train_get(eventContext, Object.class, 0, "ted");

        train_get(eventContext, Object.class, 1, "barney");

        expect(source.createPageRenderLink(PAGE_NAME, true, "ted", "barney")).andReturn(link);

        replay();

        PageRenderLinkSource service = new PageRenderLinkSourceImpl(source, resolver);

        assertSame(service.createPageRenderLinkWithContext(PAGE_CLASS, "fred", "barney"), link);

        assertSame(service.createPageRenderLinkWithContext(PAGE_CLASS, eventContext), link);

        verify();

    }

}
