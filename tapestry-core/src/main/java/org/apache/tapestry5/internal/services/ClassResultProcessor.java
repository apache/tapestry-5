// Copyright 2007, 2008, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Traditional;

import java.io.IOException;

/**
 * Used when a component event handler returns a class value. The value is interpreted as the page class. A redirect
 * to the page will be sent.
 */
public class ClassResultProcessor implements ComponentEventResultProcessor<Class>
{
    private final ComponentClassResolver resolver;

    private final ComponentEventResultProcessor primary;

    public ClassResultProcessor(ComponentClassResolver resolver,

                                @Traditional @Primary
                                ComponentEventResultProcessor primary)
    {
        this.resolver = resolver;
        this.primary = primary;
    }

    public void processResultValue(Class value) throws IOException
    {
        String className = value.getName();

        String pageName = resolver.resolvePageClassNameToPageName(className);

        primary.processResultValue(pageName);
    }

}
