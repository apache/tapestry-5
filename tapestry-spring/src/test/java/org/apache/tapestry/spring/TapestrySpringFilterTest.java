package org.apache.tapestry.spring;

import javax.servlet.ServletContext;

import org.apache.tapestry.test.TapestryTestCase;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.Test;

public class TapestrySpringFilterTest extends TapestryTestCase
{
    @Test
    public void no_web_application_context_in_servlet_context() throws Exception
    {
        ServletContext context = mockServletContext();

        expect(context.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE))
                .andReturn(null);

        replay();

        TapestrySpringFilter filter = new TapestrySpringFilter();

        try
        {
            filter.provideExtraModuleDefs(context);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "The Spring WebApplicationContext is not present. "
                            + "The likely cause is that the org.springframework.web.context.ContextLoaderListener listener was not declared "
                            + "inside the application\'s web.xml deployment descriptor.");
        }

        verify();
    }

    protected final ServletContext mockServletContext()
    {
        return newMock(ServletContext.class);
    }

    @Test
    public void failure_obtaining_context() throws Exception
    {
        ServletContext context = mockServletContext();
        Throwable t = new RuntimeException("Failure.");

        expect(context.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE))
                .andThrow(t);

        replay();

        TapestrySpringFilter filter = new TapestrySpringFilter();

        try
        {
            filter.provideExtraModuleDefs(context);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "An exception occurred obtaining the Spring WebApplicationContext: Failure.");

            assertSame(ex.getCause(), t);
        }

        verify();
    }
}
