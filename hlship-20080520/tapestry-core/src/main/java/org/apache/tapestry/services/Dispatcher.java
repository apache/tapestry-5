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

package org.apache.tapestry.services;

import java.io.IOException;

/**
 * A dispatcher is responsible for recognizing an incoming request. Dispatchers form an ordered chain of command, with
 * each dispatcher responsible for recognizing requests that it can process.
 *
 * @see org.apache.tapestry.services.TapestryModule#contributeMasterDispatcher(org.apache.tapestry.ioc.OrderedConfiguration,
 *      ClasspathAssetAliasManager, org.apache.tapestry.internal.services.ResourceCache,
 *      org.apache.tapestry.internal.services.ResourceStreamer, PageRenderRequestHandler, ComponentEventRequestHandler,
 *      ComponentClassResolver, ContextValueEncoder, String)
 */
public interface Dispatcher
{
    /**
     * Analyzes the incoming request and performs an appropriate operation for each.
     *
     * @return true if a response was delivered, false if the servlet container should be allowed to handle the request
     */
    boolean dispatch(Request request, Response response) throws IOException;
}
