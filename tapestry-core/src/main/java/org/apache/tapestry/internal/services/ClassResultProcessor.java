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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Link;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ActionResponseGenerator;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.ComponentEventResultProcessor;

/**
 * Used when a component event handler returns a class value. The value is interpreted as the page
 * class. A link to the page will be sent.
 *
 * @see LinkActionResponseGenerator
 */
public class ClassResultProcessor implements ComponentEventResultProcessor<Class>
{
    private ComponentClassResolver _resolver;

    private final RequestPageCache _requestPageCache;

    private final LinkFactory _linkFactory;

    public ClassResultProcessor(ComponentClassResolver resolver, RequestPageCache requestPageCache,
                                LinkFactory linkFactory)
    {
        _resolver = resolver;
        _requestPageCache = requestPageCache;
        _linkFactory = linkFactory;
    }

    public ActionResponseGenerator processComponentEvent(Class value, Component component,
                                                         String methodDescripion)
    {
        String className = value.getName();
        String pageName = _resolver.resolvePageClassNameToPageName(className);
        Page page = _requestPageCache.get(pageName);
        Link link = _linkFactory.createPageLink(page, false);
        return new LinkActionResponseGenerator(link);
    }

}
