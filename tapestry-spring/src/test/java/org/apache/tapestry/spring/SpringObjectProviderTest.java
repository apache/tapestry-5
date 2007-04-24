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
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.test.TapestryTestCase;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.Test;

public class SpringObjectProviderTest extends TapestryTestCase
{
  private static final String STARTUP_MESSAGE = "Using Spring WebApplicationContext containing beans: ";

  private static final String BEAN_NAME = "mySpringBean";

  @Test
  public void failure_getting_bean_from_context()
  {
    Log log = newLog();
    WebApplicationContext webContext = newWebApplicationContext();
    ServiceLocator locator = newServiceLocator();
    Throwable t = new RuntimeException("Simulated failure.");

    train_getBeanDefinitionNames(webContext, BEAN_NAME);

    log.info(STARTUP_MESSAGE + BEAN_NAME);

    expect(webContext.getBean(BEAN_NAME, SpringBean.class)).andThrow(t);

    replay();

    ObjectProvider provider = new SpringObjectProvider(log, webContext);

    try
    {
      provider.provide(BEAN_NAME, SpringBean.class, locator);
      unreachable();
    }
    catch (RuntimeException ex)
    {
      assertEquals(
          ex.getMessage(),
          "A failure occured obtaining Spring bean \'mySpringBean\' (of type org.apache.tapestry.spring.SpringBean): Simulated failure.");
      assertSame(ex.getCause(), t);
    }

    verify();
  }

  @Test
  public void get_bean_from_context()
  {
    Log log = newLog();
    WebApplicationContext webContext = newWebApplicationContext();
    ServiceLocator locator = newServiceLocator();
    SpringBean bean = newMock(SpringBean.class);

    train_getBeanDefinitionNames(webContext, "fred", "barney", BEAN_NAME);

    log.info(STARTUP_MESSAGE + "barney, fred, " + BEAN_NAME);

    expect(webContext.getBean(BEAN_NAME, SpringBean.class)).andReturn(bean);

    replay();

    ObjectProvider provider = new SpringObjectProvider(log, webContext);

    assertSame(provider.provide(BEAN_NAME, SpringBean.class, locator), bean);

    verify();
  }

  @Test
  public void bean_name_is_case_insensitive_if_in_bean_definitions()
  {
    Log log = newLog();
    WebApplicationContext webContext = newWebApplicationContext();
    ServiceLocator locator = newServiceLocator();
    SpringBean bean = newMock(SpringBean.class);

    train_getBeanDefinitionNames(webContext, "fred", "barney", BEAN_NAME);

    log.info(STARTUP_MESSAGE + "barney, fred, " + BEAN_NAME);

    expect(webContext.getBean(BEAN_NAME, SpringBean.class)).andReturn(bean);

    replay();

    ObjectProvider provider = new SpringObjectProvider(log, webContext);

    assertSame(provider.provide(BEAN_NAME.toUpperCase(), SpringBean.class, locator), bean);

    verify();
  }

  @Test
  public void bean_name_outside_of_bean_definitions_supported_with_provided_case()
  {
    Log log = newLog();
    WebApplicationContext webContext = newWebApplicationContext();
    ServiceLocator locator = newServiceLocator();
    SpringBean bean = newMock(SpringBean.class);

    train_getBeanDefinitionNames(webContext, "fred", "barney");

    log.info(STARTUP_MESSAGE + "barney, fred");

    expect(webContext.getBean(BEAN_NAME, SpringBean.class)).andReturn(bean);

    replay();

    ObjectProvider provider = new SpringObjectProvider(log, webContext);

    assertSame(provider.provide(BEAN_NAME, SpringBean.class, locator), bean);

    verify();
  }

  protected final void train_getBeanDefinitionNames(WebApplicationContext context, String... names)
  {
    expect(context.getBeanDefinitionNames()).andReturn(names);
  }

  protected final WebApplicationContext newWebApplicationContext()
  {
    return newMock(WebApplicationContext.class);
  }

}
