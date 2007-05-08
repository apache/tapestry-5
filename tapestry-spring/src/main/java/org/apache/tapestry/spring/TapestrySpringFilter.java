package org.apache.tapestry.spring;

import javax.servlet.ServletContext;

import org.apache.tapestry.TapestryFilter;
import org.apache.tapestry.internal.spring.SpringModuleDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.springframework.web.context.WebApplicationContext;

/**
 * Adds a {@link ModuleDef} that contains all the beans defined by the Spring
 * {@link WebApplicationContext}, as if they were Tapestry IoC services.
 */
public class TapestrySpringFilter extends TapestryFilter
{

    @Override
    protected ModuleDef[] provideExtraModuleDefs(ServletContext context)
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

        return new ModuleDef[]
        { new SpringModuleDef(springContext) };
    }
}
