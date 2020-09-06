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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ContextAwareException;
import org.apache.tapestry5.ExceptionHandlerAssistant;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.internal.OperationException;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ExceptionReporter;
import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class DefaultRequestExceptionHandlerTest extends InternalBaseTestCase
{
    private Map<Class, Object> mockConfiguration = new HashMap<Class, Object>();
    RequestPageCache pageCache;
    PageResponseRenderer renderer;
    Logger logger;
    Request request;
    Response response;
    ComponentClassResolver componentClassResolver;
    LinkSource linkSource;
    ServiceResources serviceResources;

    private DefaultRequestExceptionHandler exceptionHandler;


    private static class MyContextAwareException extends Throwable implements ContextAwareException
    {
        private Object[] context;

        public MyContextAwareException(Object[] context)
        {
            this.context = context;
        }

        public Object[] getContext()
        {
            return context;
        }

    }

    private static class MyPage
    {

    }

    @BeforeMethod
    public void setup_tests() throws Exception
    {
        mockConfiguration.clear();
        pageCache = mockRequestPageCache();
        renderer = mockPageResponseRenderer();
        logger = mockLogger();
        request = mockRequest();
        response = mockResponse();
        componentClassResolver = mockComponentClassResolver();
        linkSource = mockLinkSource();
        serviceResources = mockServiceResources();
        mockConfiguration.put(AccessControlException.class, MyPage.class);
        mockConfiguration.put(MyContextAwareException.class, new ExceptionHandlerAssistant()
        {
            public Object handleRequestException(Throwable exception, List<Object> exceptionContext)
                    throws IOException
            {
                return null;
            }
        });

        ExceptionReporter noopExceptionReporter = new ExceptionReporter()
        {
            @Override
            public void reportException(Throwable exception)
            {

            }
        };

        exceptionHandler = new DefaultRequestExceptionHandler(pageCache, renderer, logger, "exceptionpage", request, response, componentClassResolver, linkSource, serviceResources, noopExceptionReporter, mockConfiguration);
    }


    @Test
    public void noContextWhenExceptionDoesntContainMessage()
    {
        Object[] context = exceptionHandler.formExceptionContext(new RuntimeException()
        {
        });
        assertEquals(context.length, 0);
    }

    @Test
    public void contextIsExceptionMessage()
    {
        Object[] context = exceptionHandler.formExceptionContext(new RuntimeException()
        {
            public String getMessage()
            {
                return "HelloWorld";
            }
        });
        assertEquals(context.length, 1);
        assertTrue("helloworld".equals(context[0]));
    }

    @Test
    public void contextIsExceptionType()
    {
        Object[] context = exceptionHandler.formExceptionContext(new IllegalArgumentException("Value not allowed"));
        assertEquals(context.length, 1);
        assertTrue(context[0] instanceof String);
        assertTrue("illegalargument".equals(context[0]));
    }

    @Test
    public void contextIsProvidedByContextAwareException()
    {
        Object[] sourceContext = new Object[]{new Integer(10), this};

        Object[] context = exceptionHandler.formExceptionContext(new MyContextAwareException(sourceContext)
        {
        });
        assertEquals(context, sourceContext);

    }

    @Test
    public void handleRequestExceptionWithConfiguredPage() throws IOException
    {
        train_resolvePageClassNameToPageName(componentClassResolver, MyPage.class.getName(), "mypage");
        Link link = mockLink();
        expect(linkSource.createPageRenderLink("mypage", false, new Object[]{"accesscontrol"})).andReturn(link);
        expect(request.isXHR()).andReturn(false);
        response.sendRedirect(link);
        EasyMock.expectLastCall();
        replay();

        // also test unwrapping TapestryExceptions
        exceptionHandler.handleRequestException(new OperationException(new RenderQueueException(
                "renderqueue", new Object[0], new TapestryException("tapestryexception",
                        new AccessControlException("No permission"))), new String[0]));
    }

    @Test
    public void handleRequestExceptionWithConfiguredAssistant() throws IOException
    {
        ExceptionHandlerAssistant assistant = new ExceptionHandlerAssistant()
        {
            public Object handleRequestException(Throwable exception, List<Object> exceptionContext)
                    throws IOException
            {
                return null;
            }
        };

        mockConfiguration.put(MyContextAwareException.class, assistant);
        replay();

        exceptionHandler.handleRequestException(new MyContextAwareException(new Object[]{}));
    }

}
