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

package org.apache.tapestry5.internal.webflow.services;

import org.apache.tapestry5.internal.services.ComponentInstanceProcessor;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.Traditional;
import org.springframework.binding.expression.Expression;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.execution.ViewFactory;

public class InternalViewFactoryCreatorImpl implements InternalViewFactoryCreator
{
    private final ComponentSource componentSource;

    private final ComponentEventResultProcessor<Component> resultProcessor;

    public InternalViewFactoryCreatorImpl(
            ComponentSource componentSource,

            @Traditional @ComponentInstanceProcessor
            ComponentEventResultProcessor<Component> resultProcessor)
    {
        this.resultProcessor = resultProcessor;
        this.componentSource = componentSource;
    }

    public ViewFactory createViewFactory(final Expression viewId)
    {
        return new ViewFactory()
        {
            public View getView(RequestContext context)
            {
                String pageName = viewId.getValue(context).toString();

                Component page = componentSource.getPage(pageName);

                return new PageViewWrapper(pageName, page, resultProcessor);
            }
        };
    }
}
