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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.json.JSONObject;

/**
 * Defines an Ajax-oriented partial page render, wherein a render of a portion of a page occurs, and the content is
 * stored into a key ("content") of a {@link org.apache.tapestry5.json.JSONObject}, which is sent to the client side as
 * the final response.  Client-side JavaScript receives this reply and uses it to update a portion of the page.
 * <p/>
 * <p/>
 * The PartialMarkupRenderer service takes an ordered configuration of {@link PartialMarkupRendererFilter}s.  It can be
 * selected using the {@link org.apache.tapestry5.ioc.annotations.Primary} marker annotation.
 */
@UsesOrderedConfiguration(PartialMarkupRendererFilter.class)
public interface PartialMarkupRenderer
{
    /**
     * Implementations should perform work before or after passing the writer to the renderer.
     *
     * @param writer to which markup should be written
     * @param reply  JSONObject which will contain the partial response
     */
    void renderMarkup(MarkupWriter writer, JSONObject reply);
}
