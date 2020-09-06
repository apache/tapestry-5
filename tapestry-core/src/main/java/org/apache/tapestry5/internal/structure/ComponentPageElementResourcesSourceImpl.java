// Copyright 2008-2014 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.RequestGlobals;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.internal.services.LinkSource;
import org.apache.tapestry5.internal.services.RequestPageCache;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.apache.tapestry5.services.messages.ComponentMessagesSource;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

import java.util.Map;

public class ComponentPageElementResourcesSourceImpl implements ComponentPageElementResourcesSource
{
    private final Map<ComponentResourceSelector, ComponentPageElementResources> cache = CollectionFactory
            .newConcurrentMap();

    private final ComponentMessagesSource componentMessagesSource;

    private final TypeCoercer typeCoercer;

    private final ComponentClassCache componentClassCache;

    private final ContextValueEncoder contextValueEncoder;

    private final LinkSource linkSource;

    private final RequestPageCache requestPageCache;

    private final ComponentClassResolver componentClassResolver;

    private final LoggerSource loggerSource;

    private final OperationTracker tracker;

    private final PerthreadManager perThreadManager;

    private final boolean productionMode, componentTracingEnabled;

    private final RequestGlobals requestGlobals;

    public ComponentPageElementResourcesSourceImpl(ComponentMessagesSource componentMessagesSource,
            TypeCoercer typeCoercer, ComponentClassCache componentClassCache, ContextValueEncoder contextValueEncoder,
            LinkSource linkSource, RequestPageCache requestPageCache, ComponentClassResolver componentClassResolver,
            LoggerSource loggerSource, OperationTracker tracker, PerthreadManager perThreadManager,
            @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE) boolean productionMode,
            @Symbol(SymbolConstants.COMPONENT_RENDER_TRACING_ENABLED) boolean componentTracingEnabled,
            RequestGlobals requestGlobals)
    {
        this.componentMessagesSource = componentMessagesSource;
        this.typeCoercer = typeCoercer;
        this.componentClassCache = componentClassCache;
        this.contextValueEncoder = contextValueEncoder;
        this.linkSource = linkSource;
        this.requestPageCache = requestPageCache;
        this.componentClassResolver = componentClassResolver;
        this.loggerSource = loggerSource;
        this.tracker = tracker;
        this.perThreadManager = perThreadManager;
        this.productionMode = productionMode;
        this.componentTracingEnabled = componentTracingEnabled;
        this.requestGlobals = requestGlobals;
    }

    public ComponentPageElementResources get(ComponentResourceSelector selector)
    {
        assert selector != null;

        ComponentPageElementResources result = cache.get(selector);

        if (result == null)
        {
            result = new ComponentPageElementResourcesImpl(selector, componentMessagesSource, typeCoercer,
                    componentClassCache, contextValueEncoder, linkSource, requestPageCache, componentClassResolver,
                    loggerSource, tracker, perThreadManager, productionMode, componentTracingEnabled, requestGlobals);

            // Small race condition here, where we may create two instances of the CPER for the same locale,
            // but that's not worth worrying about.

            cache.put(selector, result);
        }

        return result;
    }
}
