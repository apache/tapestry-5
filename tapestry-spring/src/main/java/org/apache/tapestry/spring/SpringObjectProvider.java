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
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ServiceLocator;
import org.springframework.web.context.WebApplicationContext;

/**
 * Provides an object from the Spring bean context (configured in web.xml).
 * <p>
 * This is just a first pass; later we'll have the provider check to see if beans are singletons,
 * and provide a proxy to the singleton rather than the raw object itself.
 */
class SpringObjectProvider implements ObjectProvider
{
  private final WebApplicationContext _context;

  private final Map<String, String> _beanNames = newCaseInsensitiveMap();

  public SpringObjectProvider(Log log, WebApplicationContext context)
  {
    _context = context;

    // Build up a case-insensitive mapping of bean names.

    for (String name : _context.getBeanDefinitionNames())
    {
      _beanNames.put(name, name);
    }

    log.info(SpringMessages.contextStartup(_beanNames.keySet()));
  }

  /**
   * The expression is the name of a spring bean inside the context.
   */
  public <T> T provide(String expression, Class<T> objectType, ServiceLocator locator)
  {
    // Attempt to convert from the base insensitive name to the name as defined by Spring
    // (which is, to my knowledge) case sensitive.
    String effectiveName = _beanNames.containsKey(expression) ? _beanNames.get(expression)
        : expression;

    try
    {
      Object raw = _context.getBean(effectiveName, objectType);

      return objectType.cast(raw);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(SpringMessages.beanAccessFailure(effectiveName, objectType, ex),
          ex);
    }
  }

}
