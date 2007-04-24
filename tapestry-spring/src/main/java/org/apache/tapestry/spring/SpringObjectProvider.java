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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.springframework.web.context.WebApplicationContext;

/**
 * Provides an object from the Spring bean context (configured in web.xml).
 * <p>
 * This is just a first pass; later we'll have the provider check to see if beans are singletons,
 * and provide a proxy to the singleton rather than the raw object itself.
 */
public class SpringObjectProvider implements ObjectProvider
{
    private final Log _log;

    private final WebApplicationContext _context;

    private boolean _beansNamesLoaded = false;

    private final Map<String, String> _beanNames = newCaseInsensitiveMap();

    public SpringObjectProvider(Log log, @InjectService("WebApplicationContext")
    WebApplicationContext context)
    {
        _log = log;

        _context = context;
    }

    private synchronized void loadBeanNames()
    {
        if (_beansNamesLoaded) return;

        for (String name : _context.getBeanDefinitionNames())
        {
            _beanNames.put(name, name);
        }

        _log.info(SpringMessages.contextStartup(_beanNames.keySet()));

        _beansNamesLoaded = true;
    }

    /**
     * The expression is the name of a spring bean inside the context.
     */
    public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider,
            ObjectLocator locator)
    {
        SpringBean annotation = annotationProvider.getAnnotation(SpringBean.class);

        if (annotation == null) return null;

        String beanName = annotation.value();

        // Need to defer loading bean names to avoid some bootstrapping problems.

        loadBeanNames();

        // Attempt to convert from the base insensitive name to the name as defined by Spring
        // (which is, to my knowledge) case sensitive.
        String effectiveName = _beanNames.containsKey(beanName) ? _beanNames.get(beanName)
                : beanName;

        try
        {
            Object raw = _context.getBean(effectiveName, objectType);

            return objectType.cast(raw);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(SpringMessages.beanAccessFailure(
                    effectiveName,
                    objectType,
                    ex), ex);
        }
    }

}
