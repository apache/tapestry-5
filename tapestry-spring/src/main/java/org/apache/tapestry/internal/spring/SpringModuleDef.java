package org.apache.tapestry.internal.spring;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.ioc.IOCConstants;
import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.def.DecoratorDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.web.context.WebApplicationContext;

/**
 * A wrapper that converts a Spring {@link WebApplicationContext} into a set of service definitions,
 * compatible with Tapestry 5 IoC, for the beans defined in the context, as well as the context
 * itself.
 */
public class SpringModuleDef implements ModuleDef
{
    private static final String CONTEXT_SERVICE_ID = WebApplicationContext.class.getSimpleName();

    private final WebApplicationContext _context;

    private final Map<String, ServiceDef> _serviceDefs = CollectionFactory.newCaseInsensitiveMap();

    public SpringModuleDef(final WebApplicationContext context)
    {
        _context = context;

        for (final String beanName : BeanFactoryUtils.beanNamesIncludingAncestors(_context))
        {
            ServiceDef serviceDef = new ServiceDef()
            {
                private Object getBean()
                {
                    return _context.getBean(beanName);
                }

                public ObjectCreator createServiceCreator(ServiceBuilderResources resources)
                {
                    return new ObjectCreator()
                    {
                        public Object createObject()
                        {
                            return getBean();
                        }
                    };
                }

                public String getServiceId()
                {
                    return beanName;
                }

                public Class getServiceInterface()
                {
                    return getBean().getClass();
                }

                public String getServiceScope()
                {
                    return IOCConstants.DEFAULT_SCOPE;
                }

                public boolean isEagerLoad()
                {
                    return false;
                }
            };

            _serviceDefs.put(beanName, serviceDef);
        }

        // And add one service that is the Spring WebApplicationContext.

        ServiceDef serviceDef = new ServiceDef()
        {

            public ObjectCreator createServiceCreator(ServiceBuilderResources resources)
            {
                return new ObjectCreator()
                {
                    public Object createObject()
                    {
                        return _context;
                    }
                };
            }

            public String getServiceId()
            {
                return CONTEXT_SERVICE_ID;
            }

            public Class getServiceInterface()
            {
                return WebApplicationContext.class;
            }

            public String getServiceScope()
            {
                return IOCConstants.DEFAULT_SCOPE;
            }

            public boolean isEagerLoad()
            {
                return false;
            }
        };

        _serviceDefs.put(CONTEXT_SERVICE_ID, serviceDef);
    }

    public Class getBuilderClass()
    {
        return null;
    }

    /** Returns an empty set. */
    public Set<ContributionDef> getContributionDefs()
    {
        return Collections.emptySet();
    }

    /** Returns an empty set. */
    public Set<DecoratorDef> getDecoratorDefs()
    {
        return Collections.emptySet();
    }

    public String getLogName()
    {
        return "Spring";
    }

    public ServiceDef getServiceDef(String serviceId)
    {
        return _serviceDefs.get(serviceId);
    }

    public Set<String> getServiceIds()
    {
        return _serviceDefs.keySet();
    }

}
