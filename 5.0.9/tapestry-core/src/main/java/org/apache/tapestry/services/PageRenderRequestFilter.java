// Copyright 2007 The Apache Software Foundation
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

/**
 * Filter interface for {@link PageRenderRequestHandler}, which allows extra behaviors to be
 * injected into the processing of a page render request.
 */
public interface PageRenderRequestFilter
{
    /**
     * Invoked to activate and render a page. The return value of the event handler method(s) for
     * the activate event may result in an action response generator being returned.
     *
     * @param logicalPageName the logical name of the page to activate and render
     * @param context         context data, supplied by the page at render time, extracted from the render URL
     * @param handler         to delegate the invocation to
     */
    void handle(String logicalPageName, String[] context, PageRenderRequestHandler handler);
}
