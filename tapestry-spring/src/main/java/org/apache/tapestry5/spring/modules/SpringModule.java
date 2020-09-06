// Copyright 2008-2013 The Apache Software Foundation
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

package org.apache.tapestry5.spring.modules;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.http.services.ApplicationInitializer;
import org.apache.tapestry5.http.services.ApplicationInitializerFilter;
import org.apache.tapestry5.http.services.Context;
import org.apache.tapestry5.internal.spring.SymbolBeanFactoryPostProcessor;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.ioc.services.FactoryDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.modules.TapestryModule;
import org.apache.tapestry5.spring.ApplicationContextCustomizer;
import org.apache.tapestry5.spring.SpringConstants;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import javax.servlet.ServletContext;
import java.util.List;

/**
 * Module for Tapestry/Spring Integration. This module exists to force the load of the Spring ApplicationContext as part
 * of Tapestry application initialization.
 *
 * @since 5.1.0.0
 */
@ImportModule(TapestryModule.class)
public class SpringModule
{
    private final Logger logger;

    public SpringModule(Logger logger)
    {
        this.logger = logger;
    }

    @Contribute(ApplicationInitializer.class)
    @Primary
    public void reportSpringContextDetailsAtStartup(
            OrderedConfiguration<ApplicationInitializerFilter> configuration, final ApplicationContext springContext)
    {
        ApplicationInitializerFilter filter = new ApplicationInitializerFilter()
        {
            @Override
            public void initializeApplication(Context context, ApplicationInitializer initializer)
            {
                logger.info(String.format("Spring version %s with %,d defined beans.",
                        SpringVersion.getVersion(),
                        springContext.getBeanDefinitionCount()));

                initializer.initializeApplication(context);
            }
        };

        configuration.add("SpringContextInitialization", filter);
    }

    @Contribute(SymbolProvider.class)
    @FactoryDefaults
    public static void defaultExternalSpringContextOff(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(SpringConstants.USE_EXTERNAL_SPRING_CONTEXT, false);
    }

    /**
     * Defines a chain-of-command for handling application context customization. This allows the Spring context to be
     * configured before it is initially {@linkplain org.springframework.context.ConfigurableApplicationContext#refresh()
     * refreshed}.
     */
    @Marker(Primary.class)
    public static ApplicationContextCustomizer buildApplicationContextCustomizer(
            List<ApplicationContextCustomizer> configuration,
            ChainBuilder builder)
    {
        return builder.build(ApplicationContextCustomizer.class, configuration);
    }

    @Contribute(ApplicationContextCustomizer.class)
    public static void addSymbolSourceAsPropertyCustomizerForSpringBeans(
            OrderedConfiguration<ApplicationContextCustomizer> configuration,
            final SymbolSource symbolSource)
    {
        ApplicationContextCustomizer beanFactoryPostProcessorCustomizer = new ApplicationContextCustomizer()
        {

            @Override
            public void customizeApplicationContext(ServletContext servletContext,
                                                    ConfigurableWebApplicationContext applicationContext)
            {
                applicationContext.addBeanFactoryPostProcessor(new SymbolBeanFactoryPostProcessor(symbolSource));

            }
        };

        configuration.add("BeanFactoryPostProcessorCustomizer", beanFactoryPostProcessorCustomizer);
    }
}
