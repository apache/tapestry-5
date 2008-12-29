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

import org.springframework.web.context.ConfigurableWebApplicationContext;

import javax.servlet.ServletContext;

/**
 * A bridge from Spring's approach to customizing the application context, over to Tapestry's.  This is how it is
 * possible to subclass and override {@link  org.apache.tapestry5.spring.TapestrySpringFilter#customizeApplicationContext(javax.servlet.ServletContext,
 * org.springframework.web.context.ConfigurableWebApplicationContext)}.
 */
public interface ApplicationContextCustomizer
{
    /**
     * Allows the instantiated application context to be customized before it is initially {@linkplain
     * org.springframework.context.ConfigurableApplicationContext#refresh() refreshed}.
     *
     * @param servletContext
     * @param applicationContext
     */
    void customizeApplicationContext(ServletContext servletContext,
                                     ConfigurableWebApplicationContext applicationContext);
}
