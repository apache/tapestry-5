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

import static org.easymock.EasyMock.isA;

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.Link;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.annotations.Test;

public class LinkFactoryImplTest extends InternalBaseTestCase
{
    private static final String ENCODED = "*encoded*";

    private static final String PAGE_LOGICAL_NAME = "sub/MyPage";

    private static final String PAGE_CLASS_NAME = "foo.pages.sub.MyPage";

    @Test
    public void action_link_root_context_no_ids()
    {
        testActionLink(
                PAGE_CLASS_NAME,
                "",
                PAGE_LOGICAL_NAME,
                "foo.bar",
                "someaction",
                "/sub/mypage.foo.bar.someaction");
    }

    @Test
    public void action_link_root_context_with_ids()
    {
        testActionLink(
                PAGE_CLASS_NAME,
                "",
                PAGE_LOGICAL_NAME,
                "foo.bar",
                "publish",
                "/sub/mypage.foo.bar.publish/fred/5",
                "fred",
                5);
    }

    @Test
    public void action_link_named_context_no_ids()
    {
        testActionLink(
                PAGE_CLASS_NAME,
                "/fred",
                PAGE_LOGICAL_NAME,
                "foo.bar",
                "someaction",
                "/fred/sub/mypage.foo.bar.someaction");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void page_link()
    {
        Request request = newRequest();
        Response response = newResponse();
        ComponentClassResolver resolver = newComponentClassResolver();
        Page page = newPage();
        ComponentPageElement rootElement = newComponentPageElement();
        LinkFactoryListener listener = newLinkFactoryListener();
        ComponentInvocationMap map = newComponentInvocationMap();

        final Holder<Link> holder = new Holder<Link>();

        train_getName(page, PAGE_CLASS_NAME);
        train_resolvePageClassNameToPageName(resolver, PAGE_CLASS_NAME, PAGE_LOGICAL_NAME);
        train_getContextPath(request, "/barney");

        train_getRootElement(page, rootElement);

        // Answer for triggerEvent() which returns a boolean.

        IAnswer<Boolean> triggerEventAnswer = new IAnswer<Boolean>()
        {
            public Boolean answer() throws Throwable
            {
                ComponentEventHandler handler = (ComponentEventHandler) EasyMock
                        .getCurrentArguments()[2];

                handler.handleResult(new Object[]
                { "foo", "bar" }, null, null);

                return true;
            }
        };

        IAnswer<Void> createdPageLinkAnswer = new IAnswer<Void>()
        {
            public Void answer() throws Throwable
            {
                Link link = (Link) EasyMock.getCurrentArguments()[0];

                holder.put(link);

                return null;
            }
        };

        // Intercept the call to handle component event, and let the IAnswer
        // do the work.

        expect(
                rootElement.triggerEvent(
                        EasyMock.eq(TapestryConstants.PASSIVATE_EVENT),
                        (Object[]) EasyMock.isNull(),
                        EasyMock.isA(ComponentEventHandler.class))).andAnswer(triggerEventAnswer);

        train_encodeRedirectURL(
                response,
                "/barney/" + PAGE_LOGICAL_NAME.toLowerCase() + "/foo/bar",
                ENCODED);

        listener.createdPageLink(EasyMock.isA(Link.class));
        getMocksControl().andAnswer(createdPageLinkAnswer);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocation.class));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, resolver, map, null);
        factory.addListener(listener);

        Link link = factory.createPageLink(page);

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void page_link_by_name()
    {
        Request request = newRequest();
        Response response = newResponse();
        ComponentClassResolver resolver = newComponentClassResolver();
        Page page = newPage();
        ComponentPageElement rootElement = newComponentPageElement();
        LinkFactoryListener listener = newLinkFactoryListener();
        ComponentInvocationMap map = newComponentInvocationMap();
        RequestPageCache cache = newRequestPageCache();

        final Holder<Link> holder = new Holder<Link>();

        train_get(cache, PAGE_LOGICAL_NAME, page);

        train_getName(page, PAGE_CLASS_NAME);
        train_resolvePageClassNameToPageName(resolver, PAGE_CLASS_NAME, PAGE_LOGICAL_NAME);
        train_getContextPath(request, "/barney");

        train_getRootElement(page, rootElement);

        // Answer for triggerEvent() which returns a boolean.

        IAnswer<Boolean> triggerEventAnswer = new IAnswer<Boolean>()
        {
            public Boolean answer() throws Throwable
            {
                ComponentEventHandler handler = (ComponentEventHandler) EasyMock
                        .getCurrentArguments()[2];

                handler.handleResult(new Object[]
                { "foo", "bar" }, null, null);

                return true;
            }
        };

        IAnswer<Void> createdPageLinkAnswer = new IAnswer<Void>()
        {
            public Void answer() throws Throwable
            {
                Link link = (Link) EasyMock.getCurrentArguments()[0];

                holder.put(link);

                return null;
            }
        };

        // Intercept the call to handle component event, and let the IAnswer
        // do the work.

        expect(
                rootElement.triggerEvent(
                        EasyMock.eq(TapestryConstants.PASSIVATE_EVENT),
                        (Object[]) EasyMock.isNull(),
                        EasyMock.isA(ComponentEventHandler.class))).andAnswer(triggerEventAnswer);

        train_encodeRedirectURL(
                response,
                "/barney/" + PAGE_LOGICAL_NAME.toLowerCase() + "/foo/bar",
                ENCODED);

        listener.createdPageLink(EasyMock.isA(Link.class));
        getMocksControl().andAnswer(createdPageLinkAnswer);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocation.class));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, resolver, map, cache);
        factory.addListener(listener);

        Link link = factory.createPageLink(PAGE_LOGICAL_NAME);

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @SuppressWarnings("unchecked")
    private void testActionLink(String pageClassName, String contextPath, String logicalPageName,
            String nestedId, String eventName, String expectedURI, Object... context)
    {
        Request request = newRequest();
        Response response = newResponse();
        ComponentClassResolver resolver = newComponentClassResolver();
        ComponentPageElement element = newComponentPageElement();
        Page page = newPage();
        LinkFactoryListener listener = newLinkFactoryListener();
        ComponentInvocationMap map = newComponentInvocationMap();

        final Holder<Link> holder = new Holder<Link>();

        IAnswer<Void> createActionLinkAnswer = new IAnswer<Void>()
        {
            public Void answer() throws Throwable
            {
                Link link = (Link) EasyMock.getCurrentArguments()[0];

                holder.put(link);

                return null;
            }
        };

        train_getContainingPage(element, page);
        train_getName(page, pageClassName);
        train_resolvePageClassNameToPageName(resolver, pageClassName, logicalPageName);
        train_getContextPath(request, contextPath);
        train_getNestedId(element, nestedId);

        listener.createdActionLink(EasyMock.isA(Link.class));
        getMocksControl().andAnswer(createActionLinkAnswer);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocation.class));

        train_encodeURL(response, expectedURI, ENCODED);

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, resolver, map, null);
        factory.addListener(listener);

        Link link = factory.createActionLink(element, eventName, false, context);

        assertEquals(link.toURI(), ENCODED);
        assertSame(link, holder.get());

        verify();
    }
}
