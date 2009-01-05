// Copyright 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.spring.ApplicationContextCustomizer;
import org.apache.tapestry5.spring.TapestryApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;

import javax.servlet.ServletContext;

public class CustomizingContextLoader extends ContextLoader
{
    private final ApplicationContextCustomizer customizer;

    public CustomizingContextLoader(ApplicationContextCustomizer customizer)
    {
        this.customizer = customizer;
    }

    @Override
    protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext applicationContext)
    {
        customizer.customizeApplicationContext(servletContext, applicationContext);
    }

    @Override
    protected Class determineContextClass(ServletContext servletContext) throws ApplicationContextException
    {
        String contextClassName = servletContext.getInitParameter(CONTEXT_CLASS_PARAM);

        if (InternalUtils.isNonBlank(contextClassName))
        {
            Class result = super.determineContextClass(servletContext);

            if (!TapestryApplicationContext.class.isAssignableFrom(result))
                throw new IllegalArgumentException(String.format(
                        "When using the Tapestry/Spring integration library, you must specifiy a context class that extends from %s. Class %s does not. Update the '%s' servlet context init parameter.",
                        TapestryApplicationContext.class.getName(),
                        result.getName(),
                        CONTEXT_CLASS_PARAM));

            return result;
        }

        return TapestryApplicationContext.class;
    }
}
