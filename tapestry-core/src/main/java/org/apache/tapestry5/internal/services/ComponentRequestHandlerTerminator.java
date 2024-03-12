// Copyright 2009, 2024 Apache Software Foundation
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

import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.services.ComponentEventRequestHandler;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Traditional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Terminator for the {@link org.apache.tapestry5.services.ComponentRequestHandler} pipeline, that feeds out into the
 * {@link org.apache.tapestry5.services.ComponentEventRequestHandler} and {@link org.apache.tapestry5.services.PageRenderRequestHandler}
 * pipelines.
 *
 * @since 5.1.0.0
 */
public class ComponentRequestHandlerTerminator implements ComponentRequestHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentRequestHandlerTerminator.class);
    
    private final ComponentEventRequestHandler componentEventRequestHandler;

    private final PageRenderRequestHandler pageRenderRequestHandler;
    
    private final InvalidationEventHub invalidationEventHub;
    
    private final ComponentDependencyRegistry componentDependencyRegistry;

    public ComponentRequestHandlerTerminator(@Traditional ComponentEventRequestHandler componentEventRequestHandler,
                                             PageRenderRequestHandler pageRenderRequestHandler,
                                             final @ComponentClasses InvalidationEventHub invalidationEventHub,
                                             final ComponentDependencyRegistry componentDependencyRegistry)
    {
        this.componentEventRequestHandler = componentEventRequestHandler;
        this.pageRenderRequestHandler = pageRenderRequestHandler;
        this.invalidationEventHub = invalidationEventHub;
        this.componentDependencyRegistry = componentDependencyRegistry;
    }

    public void handleComponentEvent(ComponentEventRequestParameters parameters) throws IOException
    {
        boolean retry = run(() -> componentEventRequestHandler.handle(parameters));
        if (retry)
        {
            componentEventRequestHandler.handle(parameters);
        }
    }

    public void handlePageRender(PageRenderRequestParameters parameters) throws IOException
    {
        boolean retry = run(() -> pageRenderRequestHandler.handle(parameters));
        if (retry)
        {
            pageRenderRequestHandler.handle(parameters);
        }
    }
    
    private static final Pattern PATTERN = Pattern.compile(
          "class (\\S+) cannot be cast to class (\\S+).*");
    
    private static interface RunnableWithIOException
    {
        public void run() throws IOException;
    }
    
    private boolean run(RunnableWithIOException runnable) throws IOException
    {
        boolean retry = false;
        try {
            runnable.run();
        }
        catch (RuntimeException e)
        {
            Throwable throwable = e;
            while (!(throwable instanceof ClassCastException) && throwable.getCause() != null)
            {
                throwable = throwable.getCause();
            }
            if (throwable instanceof ClassCastException && throwable != null)
            {
                Matcher matcher = PATTERN.matcher(throwable.getMessage());
                if (matcher.matches() && matcher.groupCount() >= 2 && 
                        isTransformed(matcher.group(1)) &&
                        isTransformed(matcher.group(2))) 
                {
                    LOGGER.warn("Caught exception and trying to recover by invalidating generated classes caches: {}", 
                            throwable.getMessage());
                    componentDependencyRegistry.disableInvalidations();
                    invalidationEventHub.fireInvalidationEvent(Collections.emptyList());
                    componentDependencyRegistry.enableInvalidations();
                    retry = true;
                }
            }
            else 
            {
                throw e;
            }
        }
        return retry;
    }

    /**
     * Very simple, but fast, implementation.
     */
    private boolean isTransformed(String className) 
    {
        return className != null && (
                className.contains(".pages.") || 
                className.contains(".components.") || 
                className.contains(".base."));
                
    }
    
}
