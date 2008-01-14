// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ComponentEventResultProcessor;

import java.io.IOException;

/**
 * Used when a component event handler returns a string value. The value is interpreted as the logical name of a page. A
 * link to the page will be sent as a redirect.
 */
public class StringResultProcessor implements ComponentEventResultProcessor<String>
{
    private final RequestPageCache _requestPageCache;

    private final ActionRenderResponseGenerator _generator;

    public StringResultProcessor(RequestPageCache requestPageCache, ActionRenderResponseGenerator generator)
    {
        _requestPageCache = requestPageCache;
        _generator = generator;
    }

    public void processComponentEvent(String value, Component component, String methodDescripion) throws IOException
    {
        Page page = _requestPageCache.get(value);

        _generator.generateResponse(page);
    }

}
