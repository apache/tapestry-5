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

import org.apache.tapestry5.TapestryFilter;
import org.apache.tapestry5.internal.spring.SpringModuleDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

/**
 * Adds a {@link ModuleDef} that contains all the beans defined by the Spring {@link ApplicationContext}, as if they
 * were Tapestry IoC services. This is done using a filter, so that the Spring beans can be "mixed into" the Tapestry
 * IoC Registry before it even starts up.
 */
public class TapestrySpringFilter extends TapestryFilter
{
    @Override
    protected ModuleDef[] provideExtraModuleDefs(ServletContext context)
    {
        ApplicationContext springContext = null;

        try
        {
            springContext = (ApplicationContext) context
                    .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(SpringMessages.failureObtainingContext(ex), ex);
        }

        if (springContext == null) throw new RuntimeException(SpringMessages.missingContext());

        return new ModuleDef[]
                { new SpringModuleDef(springContext) };
    }
}
