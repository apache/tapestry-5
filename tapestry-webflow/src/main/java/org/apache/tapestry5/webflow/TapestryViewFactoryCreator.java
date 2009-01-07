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

package org.apache.tapestry5.webflow;

import org.apache.tapestry5.internal.webflow.services.InternalViewFactoryCreator;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.ViewFactoryCreator;
import org.springframework.webflow.execution.ViewFactory;

public class TapestryViewFactoryCreator implements ViewFactoryCreator
{

    private final InternalViewFactoryCreator internalCreator;

    @Autowired
    public TapestryViewFactoryCreator(@Inject InternalViewFactoryCreator internalCreator)
    {
        this.internalCreator = internalCreator;
    }

    public ViewFactory createViewFactory(Expression viewId, ExpressionParser expressionParser,
                                         ConversionService conversionService, BinderConfiguration binderConfiguration)
    {
        return internalCreator.createViewFactory(viewId);
    }

    /**
     * We map view state ids directly to page names, so this method just returns the viewStateId unchanged.
     */
    public String getViewIdByConvention(String viewStateId)
    {
        return viewStateId;
    }
}
