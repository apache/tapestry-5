// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.IdMatcher;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Abstract base class for implementations of {@link org.apache.tapestry5.ioc.ServiceDecorator} (i.e., old school) and
 * {@link org.apache.tapestry5.ioc.ServiceAdvisor} (i.e., new school).  "Instrumenter" is a rought approximation of what
 * these two approaches have in common: instrumenting of method calls of a service.
 *
 * @since 5.1.0.0
 */
public class AbstractServiceInstrumenter
{
    protected final Method method;

    protected final IdMatcher idMatcher;

    protected final String[] constraints;

    protected final ClassFactory classFactory;

    public AbstractServiceInstrumenter(Method method, String[] patterns, String[] constraints,
                                       ClassFactory classFactory)
    {
        this.method = method;
        this.classFactory = classFactory;

        List<IdMatcher> matchers = CollectionFactory.newList();

        for (String pattern : Defense.notNull(patterns, "patterns"))
        {
            IdMatcher matcher = new IdMatcherImpl(pattern);
            matchers.add(matcher);
        }

        idMatcher = new OrIdMatcher(matchers);

        this.constraints = constraints != null ? constraints : new String[0];
    }

    @Override
    public String toString()
    {
        return InternalUtils.asString(method, classFactory);
    }

    public String[] getConstraints()
    {
        return constraints;
    }

    /**
     * Returns true if <em>any</em> provided pattern matches the id of the service.
     */
    public boolean matches(ServiceDef serviceDef)
    {
        String serviceId = serviceDef.getServiceId();

        return idMatcher.matches(serviceId);
    }
}
