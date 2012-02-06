package org.apache.tapestry5.internal.services;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ContextAwareException;
import org.apache.tapestry5.ExceptionHandlerAssistant;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.easymock.EasyMock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SuppressWarnings("serial")
public class DefaultRequestExceptionHandlerTest extends InternalBaseTestCase {
    private Map<Class, Object> mockConfiguration = new HashMap<Class, Object>();
    RequestPageCache pageCache = mockRequestPageCache();
    PageResponseRenderer renderer = mockPageResponseRenderer();
    Logger logger = mockLogger();
    Request request = mockRequest();
    Response response = mockResponse();
    ComponentClassResolver componentClassResolver = mockComponentClassResolver();
    LinkSource linkSource = mockLinkSource();
	private DefaultRequestExceptionHandler exceptionHandler = new DefaultRequestExceptionHandler(pageCache, renderer, logger, "exceptionpage", request, response, componentClassResolver, linkSource, mockConfiguration);

	private static class MyContextAwareException extends Throwable implements ContextAwareException {
		private Object[] context;

		public MyContextAwareException(Object[] context) {
			this.context = context;
		}

		public Object[] getContext() {
			return context;
		}

	}
	
	private static class MyPage {
	    
	}
	
    @BeforeClass
    public void setup_tests() throws Exception
    {
        mockConfiguration.clear();
    }
	

	@Test
	public void noContextWhenExceptionDoesntContainMessage() {
		Object[] context = exceptionHandler.formExceptionContext(new RuntimeException() {
		});
		assertEquals(context.length, 0);
	}

	@Test
	public void contextIsExceptionMessage() {
		Object[] context = exceptionHandler.formExceptionContext(new RuntimeException() {
			public String getMessage() {
				return "HelloWorld";
			}
		});
		assertEquals(context.length, 1);
		assertTrue("helloworld".equals(context[0]));
	}

	@Test
	public void contextIsExceptionType() {
		Object[] context = exceptionHandler.formExceptionContext(new IllegalArgumentException("Value not allowed"));
		assertEquals(context.length, 1);
		assertTrue(context[0] instanceof String);
		assertTrue("illegalargument".equals(context[0]));
	}

	@Test
	public void contextIsProvidedByContextAwareException() {
		Object[] sourceContext = new Object[] { new Integer(10), this };

		Object[] context = exceptionHandler.formExceptionContext(new MyContextAwareException(sourceContext) {
		});
		assertEquals(context, sourceContext);

	}
	
	@Test
	public void handleRequestExceptionWithConfiguredPage() throws IOException {
	    mockConfiguration.put(AccessControlException.class, MyPage.class);
	    train_resolvePageClassNameToPageName(componentClassResolver, MyPage.class.getName(), "mypage" );
	    Link link = mockLink();
        expect(linkSource.createPageRenderLink("mypage", false, new Object[]{"accesscontrol"})).andReturn(link);
        expect(request.isXHR()).andReturn(false);
        response.sendRedirect(link);
        EasyMock.expectLastCall();
        replay();
	    
	    exceptionHandler.handleRequestException(new AccessControlException("No permission"));
	}
	
    @Test
    public void handleRequestExceptionWithConfiguredAssistant() throws IOException {
        ExceptionHandlerAssistant assistant = new ExceptionHandlerAssistant() {
            @Override
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
