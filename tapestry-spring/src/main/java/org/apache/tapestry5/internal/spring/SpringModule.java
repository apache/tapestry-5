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

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.services.ApplicationInitializer;
import org.apache.tapestry5.services.ApplicationInitializerFilter;
import org.apache.tapestry5.services.Context;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringVersion;

/**
 * Module for Tapestry/Spring Integration. This module exists to force the load of the Spring ApplicationContext as part
 * of Tapestry application initialization.
 */
public class SpringModule
{
    private final Logger logger;

    public SpringModule(Logger logger)
    {
        this.logger = logger;
    }

    public void contributeApplicationInitializer(
            OrderedConfiguration<ApplicationInitializerFilter> configuration, final ApplicationContext springContext)
    {
        ApplicationInitializerFilter filter = new ApplicationInitializerFilter()
        {
            public void initializeApplication(Context context, ApplicationInitializer initializer)
            {
                logger.info(String.format("Spring version %s with %,d defined beans.",
                                          SpringVersion.getVersion(),
                                          springContext.getBeanDefinitionCount()));
            }
        };

        configuration.add("SpringContextInitialization", filter);
    }
}
