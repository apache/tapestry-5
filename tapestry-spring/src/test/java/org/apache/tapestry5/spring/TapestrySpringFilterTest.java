// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.spring;

import org.apache.tapestry5.test.TapestryTestCase;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.Test;

import javax.servlet.ServletContext;

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
