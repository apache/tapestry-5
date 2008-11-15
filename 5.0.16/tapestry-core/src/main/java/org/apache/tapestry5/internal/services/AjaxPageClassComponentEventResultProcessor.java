//  Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.services.Ajax;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.ComponentSource;

import java.io.IOException;

/**
 * Processes a Class result by converting the Class to a logical page name, then processing that.
 *
 * @see org.apache.tapestry5.services.ComponentSource
 * @see org.apache.tapestry5.internal.services.AjaxPageNameComponentEventResultProcessor
 */
public class AjaxPageClassComponentEventResultProcessor implements ComponentEventResultProcessor<Class>
{
    private final ComponentSource componentSource;

    private final ComponentEventResultProcessor masterProcessor;

    public AjaxPageClassComponentEventResultProcessor(ComponentSource componentSource,
                                                      @Ajax ComponentEventResultProcessor masterProcessor)
    {
        this.componentSource = componentSource;
        this.masterProcessor = masterProcessor;
    }

    public void processResultValue(Class value) throws IOException
    {
        String pageName = componentSource.getPage(value).getComponentResources().getPageName();

        masterProcessor.processResultValue(pageName);
    }
}
