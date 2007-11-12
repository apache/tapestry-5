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

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.Link;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LinkFactoryImplTest extends InternalBaseTestCase
{
    private static final String ENCODED = "*encoded*";

    private static final String PAGE_LOGICAL_NAME = "sub/MyPage";

    private TypeCoercer _typeCoercer;

    @BeforeClass
    public void setup()
    {
        _typeCoercer = getObject(TypeCoercer.class, null);
    }

    @AfterClass
    public void cleanup()
    {
        _typeCoercer = null;
    }

    @Test
    public void action_link_root_context_no_ids()
    {
        testActionLink(
                "",
                PAGE_LOGICAL_NAME,
                "foo.bar",
                "someaction",
                "/sub/mypage.foo.bar:someaction");

    }

    @Test
    public void action_link_root_context_with_ids()
    {
        testActionLink(
                "",
                PAGE_LOGICAL_NAME,
                "foo.bar",
                "publish",
                "/sub/mypage.foo.bar:publish/fred/5",
                "fred",
                5);
    }

    @Test
    public void action_link_with_default_action()
    {
        testActionLink(
                "",
                PAGE_LOGICAL_NAME,
                "foo.bar",
                TapestryConstants.ACTION_EVENT,
                "/sub/mypage.foo.bar/fred/5",
                "fred",
                5);
    }

    @Test
    public void page_level_event_always_includes_action()
    {
        testActionLink(
                "",
                PAGE_LOGICAL_NAME,
                "",
                TapestryConstants.ACTION_EVENT,
                "/sub/mypage:action/barney/99",
                "barney",
                99);
    }

    @Test
    public void action_link_named_context_no_ids()
    {
        testActionLink(
                "/fred",
                PAGE_LOGICAL_NAME,
                "foo.bar",
                "someaction",
                "/fred/sub/mypage.foo.bar:someaction");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void page_link()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();

        train_getLogicalName(page, PAGE_LOGICAL_NAME);
        train_getContextPath(request, "/barney");

        train_getRootElement(page, rootElement);

        final Holder<Link> holder = new Holder<Link>();

        train_triggerPassivateEventForPageLink(rootElement, listener, holder);

        train_encodeRedirectURL(
                response,
                "/barney/" + PAGE_LOGICAL_NAME.toLowerCase() + "/foo/bar",
                ENCODED);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocation.class));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, null, _typeCoercer);
        factory.addListener(listener);

        Link link = factory.createPageLink(page, false);

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void page_link_using_supplied_activation_context()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();

        train_getLogicalName(page, PAGE_LOGICAL_NAME);
        train_getContextPath(request, "/barney");

        Holder<Link> holder = new Holder<Link>();

        IAnswer<Void> createdPageLinkAnswer = newAnswerForCreatedLink(holder);

        listener.createdPageLink(isA(Link.class));
        getMocksControl().andAnswer(createdPageLinkAnswer);

        train_encodeRedirectURL(response, "/barney/" + PAGE_LOGICAL_NAME.toLowerCase()
                + "/biff/bazz", ENCODED);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocation.class));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, null, _typeCoercer);
        factory.addListener(listener);

        Link link = factory.createPageLink(page, false, "biff", "bazz");

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void page_link_using_empty_activation_context_and_override()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();

        train_getLogicalName(page, PAGE_LOGICAL_NAME);
        train_getContextPath(request, "/barney");

        Holder<Link> holder = new Holder<Link>();

        IAnswer<Void> createdPageLinkAnswer = newAnswerForCreatedLink(holder);

        listener.createdPageLink(isA(Link.class));
        getMocksControl().andAnswer(createdPageLinkAnswer);

        train_encodeRedirectURL(response, "/barney/" + PAGE_LOGICAL_NAME.toLowerCase(), ENCODED);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocation.class));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, null, _typeCoercer);
        factory.addListener(listener);

        Link link = factory.createPageLink(page, true);

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @Test
    public void page_link_by_name()
    {
        Request request = mockRequest();
        Response response = mockResponse();
        Page page = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();
        RequestPageCache cache = mockRequestPageCache();

        train_get(cache, PAGE_LOGICAL_NAME, page);

        train_getLogicalName(page, PAGE_LOGICAL_NAME);
        train_getContextPath(request, "/barney");

        train_getRootElement(page, rootElement);

        // Answer for triggerEvent() which returns a boolean.
        final Holder<Link> holder = new Holder<Link>();

        train_triggerPassivateEventForPageLink(rootElement, listener, holder);

        train_encodeRedirectURL(
                response,
                "/barney/" + PAGE_LOGICAL_NAME.toLowerCase() + "/foo/bar",
                ENCODED);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocation.class));

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, cache, _typeCoercer);
        factory.addListener(listener);

        Link link = factory.createPageLink(PAGE_LOGICAL_NAME, false);

        assertEquals(link.toRedirectURI(), ENCODED);

        // Make sure the link was passed to the LinkFactoryListener

        assertSame(link, holder.get());

        verify();
    }

    @SuppressWarnings("unchecked")
    private void train_triggerPassivateEventForPageLink(ComponentPageElement rootElement,
                                                        LinkFactoryListener listener, Holder<Link> holder)
    {
        IAnswer<Boolean> triggerEventAnswer = newAnswerForPassivateEventTrigger();

        IAnswer<Void> createdPageLinkAnswer = newAnswerForCreatedLink(holder);

        // Intercept the call to handle component event, and let the IAnswer
        // do the work.

        expect(
                rootElement.triggerEvent(
                        eq(TapestryConstants.PASSIVATE_EVENT),
                        (Object[]) isNull(),
                        isA(ComponentEventHandler.class))).andAnswer(triggerEventAnswer);

        listener.createdPageLink(isA(Link.class));
        getMocksControl().andAnswer(createdPageLinkAnswer);
    }

    @SuppressWarnings("unchecked")
    private void train_triggerPassivateEventForActionLink(ComponentPageElement rootElement,
                                                          LinkFactoryListener listener, Holder<Link> holder)
    {
        IAnswer<Boolean> triggerEventAnswer = newAnswerForPassivateEventTrigger();

        IAnswer<Void> createdPageLinkAnswer = newAnswerForCreatedLink(holder);

        // Intercept the call to handle component event, and let the IAnswer
        // do the work.

        expect(
                rootElement.triggerEvent(
                        eq(TapestryConstants.PASSIVATE_EVENT),
                        (Object[]) isNull(),
                        isA(ComponentEventHandler.class))).andAnswer(triggerEventAnswer);

        listener.createdActionLink(isA(Link.class));
        getMocksControl().andAnswer(createdPageLinkAnswer);
    }

    private IAnswer<Void> newAnswerForCreatedLink(final Holder<Link> holder)
    {
        return new IAnswer<Void>()
        {
            public Void answer() throws Throwable
            {
                Link link = (Link) EasyMock.getCurrentArguments()[0];

                holder.put(link);

                return null;
            }
        };
    }

    private IAnswer<Boolean> newAnswerForPassivateEventTrigger()
    {
        return new IAnswer<Boolean>()
        {
            @SuppressWarnings("unchecked")
            public Boolean answer() throws Throwable
            {
                ComponentEventHandler handler = (ComponentEventHandler) EasyMock
                        .getCurrentArguments()[2];

                handler.handleResult(new Object[]
                        {"foo", "bar"}, null, null);

                return true;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private void testActionLink(String contextPath, String logicalPageName, String nestedId,
                                String eventName, String expectedURI, Object... context)
    {
        Request request = mockRequest();
        Response response = mockResponse();
        ComponentPageElement element = mockComponentPageElement();
        Page page = mockPage();
        ComponentPageElement rootElement = mockComponentPageElement();
        LinkFactoryListener listener = mockLinkFactoryListener();
        ComponentInvocationMap map = mockComponentInvocationMap();
        RequestPageCache cache = mockRequestPageCache();

        final Holder<Link> holder = new Holder<Link>();

        train_getContainingPage(element, page);
        train_getLogicalName(page, logicalPageName);
        train_getContextPath(request, contextPath);
        train_getNestedId(element, nestedId);

        train_getRootElement(page, rootElement);
        train_triggerPassivateEventForActionLink(rootElement, listener, holder);

        // This needs to be refactored a bit to be more testable.

        map.store(isA(Link.class), isA(ComponentInvocation.class));

        train_encodeURL(response, String.format(
                "%s?%s=foo/bar",
                expectedURI,
                InternalConstants.PAGE_CONTEXT_NAME), ENCODED);

        replay();

        LinkFactory factory = new LinkFactoryImpl(request, response, map, cache, _typeCoercer);
        factory.addListener(listener);

        Link link = factory.createActionLink(element, eventName, false, context);

        assertEquals(link.toURI(), ENCODED);
        assertSame(link, holder.get());

        verify();
    }
}
