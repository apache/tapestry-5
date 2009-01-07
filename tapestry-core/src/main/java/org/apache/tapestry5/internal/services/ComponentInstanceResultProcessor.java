// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Traditional;
import org.slf4j.Logger;

import java.io.IOException;

@Marker({Traditional.class, ComponentInstanceProcessor.class})
public class ComponentInstanceResultProcessor implements ComponentEventResultProcessor<Component>
{
    private final RequestPageCache requestPageCache;

    private final Logger logger;

    private final ActionRenderResponseGenerator generator;

    public ComponentInstanceResultProcessor(Logger logger, RequestPageCache requestPageCache,
                                            ActionRenderResponseGenerator generator)
    {
        this.requestPageCache = requestPageCache;
        this.logger = logger;
        this.generator = generator;
    }

    public void processResultValue(Component value) throws IOException
    {
        ComponentResources resources = value.getComponentResources();

        if (resources.getContainer() != null)
            logger.warn(ServicesMessages.componentInstanceIsNotAPage(value));

        // We have all these layers and layers between us and the page instance, but its easy to
        // extract the page class name and quickly re-resolve that to the page instance.

        Page page = requestPageCache.get(resources.getPageName());

        generator.generateResponse(page);
    }
}
