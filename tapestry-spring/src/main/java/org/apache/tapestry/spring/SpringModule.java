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

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.services.Context;
import org.springframework.web.context.WebApplicationContext;

public class SpringModule
{
    /**
     * Provider that interprets expressions as the names of beans within the Spring
     * {@link WebApplicationContext}.
     */
    public static ObjectProvider buildSpringObjectProvider(Log log,
            @InjectService("WebApplicationContext")
            WebApplicationContext context)
    {
        return new SpringObjectProvider(log, context);
    }

    /**
     * Obtains and returns the Spring WebApplicationContext, which is stored in the Servlet context
     * using a well-known name. The WebApplicationContext must be configured in the <a
     * href="http://static.springframework.org/spring/docs/1.2.x/reference/beans.html#context-create">standard
     * way</a> (which involves adding a listener to the web.xml deployment descriptor).
     */
    public static WebApplicationContext build(@Inject("alias:context")
    Context context)
    {
        WebApplicationContext springContext = null;

        try
        {
            springContext = (WebApplicationContext) context
                    .getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(SpringMessages.failureObtainingContext(ex), ex);
        }

        if (springContext == null) throw new RuntimeException(SpringMessages.missingContext());

        return springContext;
    }

    /**
     * Contributes a "spring:" provider, that obtains beans from the
     * {@link #buildSpringObjectProvider(Log, Context) SpringObjectProvider service}.
     */
    public static void contributeMasterObjectProvider(@InjectService("SpringObjectProvider")
    ObjectProvider springObjectProvider,

    MappedConfiguration<String, ObjectProvider> configuration)
    {
        configuration.add("spring", springObjectProvider);
    }

}
