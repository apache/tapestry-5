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

package org.apache.tapestry5.spring;

import org.apache.tapestry5.TapestryFilter;
import org.apache.tapestry5.internal.spring.TapestyBeanFactory;
import org.apache.tapestry5.ioc.Registry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Extension of Spring's {@link org.springframework.web.context.support.XmlWebApplicationContext} that includes hooks to
 * resolve some injections into Spring beans as Tapestry services. When using the Tapestry/Spring Integration Library,
 * this class (or a subclass) must be the configured context class.  If not specified, this  class is the default
 * context class.
 */
public class TapestryApplicationContext extends XmlWebApplicationContext
{
    @Override
    protected DefaultListableBeanFactory createBeanFactory()
    {
        Registry registry = (Registry) getServletContext().getAttribute(TapestryFilter.REGISTRY_CONTEXT_NAME);

        if (registry == null)
            throw new IllegalStateException(
                    "Expected a Tapestry IoC Registry to be stored in the ServletContext, but the attribute was null.");

        return new TapestyBeanFactory(getInternalParentBeanFactory(), registry);
    }
}
