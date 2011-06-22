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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.ComponentClassResolver;

public class ComponentModelSourceImpl implements ComponentModelSource
{
    private final ComponentClassResolver resolver;

    private final ComponentInstantiatorSource source;

    public ComponentModelSourceImpl(ComponentClassResolver resolver, ComponentInstantiatorSource source)
    {
        this.resolver = resolver;
        this.source = source;
    }

    public ComponentModel getModel(String componentClassName)
    {
        return source.getInstantiator(componentClassName).getModel();
    }

    public ComponentModel getPageModel(String pageName)
    {
        return getModel(resolver.resolvePageNameToClassName(pageName));
    }
}
