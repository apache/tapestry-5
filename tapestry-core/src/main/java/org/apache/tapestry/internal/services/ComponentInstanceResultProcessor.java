// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Link;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ActionResponseGenerator;
import org.apache.tapestry.services.ComponentEventResultProcessor;
import org.slf4j.Logger;

public class ComponentInstanceResultProcessor implements ComponentEventResultProcessor<Component>
{
    private final RequestPageCache _requestPageCache;

    private final LinkFactory _linkFactory;

    private final Logger _logger;

    public ComponentInstanceResultProcessor(final RequestPageCache requestPageCache,
            LinkFactory linkFactory, Logger logger)
    {
        _requestPageCache = requestPageCache;
        _linkFactory = linkFactory;
        _logger = logger;
    }

    public ActionResponseGenerator processComponentEvent(Component value, Component component,
            String methodDescription)
    {
        ComponentResources resources = value.getComponentResources();

        if (resources.getContainer() != null)
            _logger.warn(ServicesMessages.componentInstanceIsNotAPage(
                    methodDescription,
                    component,
                    value));

        // We have all these layers and layers between us and the page instance, but its easy to
        // extract the page class name and quickly re-resolve that to the page instance.

        Page page = _requestPageCache.get(resources.getPageName());

        Link link = _linkFactory.createPageLink(page, false);

        return new LinkActionResponseGenerator(link);
    }
}
