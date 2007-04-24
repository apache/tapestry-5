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

package org.apache.tapestry.spring;

import org.apache.tapestry.services.Context;
import org.apache.tapestry.test.TapestryTestCase;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.Test;

public class SpringModuleTest extends TapestryTestCase
{

    @Test
    public void missing_spring_context()
    {
        Context context = newContext(null);

        replay();

        try
        {
            SpringModule.build(context);

            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "The Spring WebApplicationContext is not present. The likely cause is that the org.springframework.web.context.ContextLoaderListener listener was not declared inside the application\'s web.xml deployment descriptor.");
        }

        verify();
    }

    @Test
    public void success()
    {
        WebApplicationContext webContext = newMock(WebApplicationContext.class);
        Context context = newContext(webContext);

        replay();

        assertSame(SpringModule.build(context), webContext);

        verify();
    }

    protected final Context newContext(Object webApplicationContext)
    {
        Context context = newContext();

        expect(context.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE))
                .andReturn(webApplicationContext);

        return context;
    }

    @Test
    public void error_getting_spring_context()
    {
        Context context = newContext("[Placeholder]");

        replay();

        try
        {
            SpringModule.build(context);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().startsWith(
                    "An exception occurred obtaining the Spring WebApplicationContext"));
        }

        verify();
    }

}
