// Copyright 2006, 2007, 2008, 2009, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Traditional;
import org.slf4j.Logger;

import java.io.IOException;

@Marker({Traditional.class, ComponentInstanceProcessor.class})
public class ComponentInstanceResultProcessor implements ComponentEventResultProcessor<Component>
{
    private final Logger logger;

    private final ComponentEventResultProcessor resultProcessor;

    public ComponentInstanceResultProcessor(Logger logger,
                                            @Traditional @Primary ComponentEventResultProcessor resultProcessor)
    {
        this.logger = logger;
        this.resultProcessor = resultProcessor;
    }

    public void processResultValue(Component value) throws IOException
    {
        ComponentResources resources = value.getComponentResources();

        if (resources.getContainer() != null)
        {
            logger.warn("Component {} was returned from an event handler method, but is not a page component. The page containing the component will render the client response.", value.getComponentResources().getCompleteId());
        }

        resultProcessor.processResultValue(resources.getPageName());
    }
}
