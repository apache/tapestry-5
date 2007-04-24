// Copyright 2006 The Apache Software Foundation
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

import org.apache.commons.logging.Log;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Link;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ActionResponseGenerator;
import org.apache.tapestry.services.ComponentEventResultProcessor;

public class ComponentInstanceResultProcessor implements ComponentEventResultProcessor<Component>
{
    private final RequestPageCache _requestPageCache;

    private final LinkFactory _linkFactory;

    private final Log _log;

    public ComponentInstanceResultProcessor(final RequestPageCache requestPageCache,
            LinkFactory linkFactory, Log log)
    {
        _requestPageCache = requestPageCache;
        _linkFactory = linkFactory;
        _log = log;
    }

    public ActionResponseGenerator processComponentEvent(Component value, Component component,
            String methodDescription)
    {
        ComponentResources resources = value.getComponentResources();

        if (resources.getContainer() != null)
        {
            _log.warn(ServicesMessages.componentInstanceIsNotAPage(
                    methodDescription,
                    component,
                    value));

            resources = resources.getPage().getComponentResources();
        }

        // We have all these layers and layers between us and the page instance, but its easy to
        // extract the page class name and quickly re-resolve that to the page instance.

        String pageClassName = resources.getCompleteId();

        Page page = _requestPageCache.getByClassName(pageClassName);

        Link link = _linkFactory.createPageLink(page);

        return new LinkActionResponseGenerator(link);
    }
}
