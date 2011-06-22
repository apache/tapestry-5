// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.spring;

import org.apache.tapestry5.spring.SpringTestCase;
import org.apache.tapestry5.spring.TapestryApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.testng.annotations.Test;

import javax.servlet.ServletContext;

public class CustomizingContextLoaderTest extends SpringTestCase
{
    @Test
    public void specified_context_class_is_not_compatible()
    {
        ServletContext context = mockServletContext();

        train_getInitParameter(context, ContextLoader.CONTEXT_CLASS_PARAM, XmlWebApplicationContext.class.getName());

        replay();

        CustomizingContextLoader ccl = new CustomizingContextLoader(null);

        try
        {
            ccl.determineContextClass(context);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertMessageContains(ex,
                                  "context class that extends from org.apache.tapestry5.spring.TapestryApplicationContext",
                                  "Class org.springframework.web.context.support.XmlWebApplicationContext does not.",
                                  "Update the 'contextClass' servlet context init parameter.");
        }

        verify();
    }

    @Test
    public void specified_context_class_is_compatible()
    {
        ServletContext context = mockServletContext();

        train_getInitParameter(context, ContextLoader.CONTEXT_CLASS_PARAM, TapestryApplicationContext.class.getName());

        replay();

        CustomizingContextLoader ccl = new CustomizingContextLoader(null);

        assertSame(ccl.determineContextClass(context), TapestryApplicationContext.class);

        verify();
    }
}
