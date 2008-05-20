// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import java.io.IOException;

/**
 * Filter interface for {@link PageRenderRequestHandler}, which allows extra behaviors to be injected into the
 * processing of a page render request.
 */
public interface PageRenderRequestFilter
{
    /**
     * Invoked to activate and render a page. The return value of the event handler method(s) for the activate event may
     * result in an action response generator being returned.
     *
     * @param parameters defines the page name and activation context
     * @param handler    to delegate the invocation to
     */
    void handle(PageRenderRequestParameters parameters, PageRenderRequestHandler handler) throws IOException;
}
