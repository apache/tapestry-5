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

package org.apache.tapestry5.webflow.services;

import org.apache.tapestry5.internal.webflow.services.*;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Dispatcher;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.executor.FlowExecutionResult;

public class WebFlowModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(FlowManager.class, FlowManagerImpl.class);
        binder.bind(InternalViewFactoryCreator.class, InternalViewFactoryCreatorImpl.class);
        binder.bind(ExternalContextSource.class, ExternalContextSourceImpl.class);
        binder.bind(FlowUrlHandler.class, TapestryFlowURLHandler.class);
        binder.bind(InternalFlowManager.class, InternalFlowManagerImpl.class);
    }

    public static void contributeMasterDispatcher(OrderedConfiguration<Dispatcher> configuration)
    {
        configuration.addInstance("WebFlow", WebflowDispatcher.class, "before:ComponentEvent,PageRender");
    }

    public static void contributeComponentEventResultProcessor(
            MappedConfiguration<Class, ComponentEventResultProcessor> configuration)
    {
        configuration.addInstance(FlowExecutionResult.class, FlowExecutionResultProcessor.class);
    }
}
