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

package org.apache.tapestry5.services;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.json.JSONObject;

/**
 * A filter (the main interface being {@link PartialMarkupRenderer}) applied when performing a partial page render as
 * part of an Ajax-oriented request.  This is similar to {@link org.apache.tapestry5.services.MarkupRendererFilter} and
 * filters are often in place so as to contribute {@link org.apache.tapestry5.annotations.Environmental} services to the
 * pages and components that render.
 *
 * @see org.apache.tapestry5.services.TapestryModule#contributePartialMarkupRenderer(org.apache.tapestry5.ioc.OrderedConfiguration,
 *      org.apache.tapestry5.Asset, org.apache.tapestry5.ioc.services.SymbolSource, AssetSource,
 *      ValidationMessagesSource)
 */
public interface PartialMarkupRendererFilter
{
    /**
     * Implementations should perform work before or after passing the writer to the renderer.
     *
     * @param writer   to which markup should be written
     * @param reply    JSONObject which will contain the partial response
     * @param renderer delegate to which the writer should be passed
     */
    void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer);
}
