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

import org.apache.tapestry.MarkupWriter;

/**
 * Filter interface for {@link org.apache.tapestry.services.MarkupRenderer}, which allows
 * for code to execute before and/or after the main rendering process.  Typically, this is to
 * allow for the placement of {@linkplain org.apache.tapestry.services.Environment environmental services}.
 */
public interface MarkupRendererFilter
{
    /**
     * Implementations should perform work before or after passing the writer
     * to the renderer.
     *
     * @param writer   to which markup should be written
     * @param renderer delegate to which the writer should be passed.
     */
    void renderMarkup(MarkupWriter writer, MarkupRenderer renderer);
}
