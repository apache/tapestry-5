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

/**
 * An object which will perform rendering of a page (or portion of a page).  This interface exists to be filtered via
 * {@link org.apache.tapestry5.services.MarkupRendererFilter}.
 * <p/>
 * The MarkupRenderer service takes an ordered configuration of {@link org.apache.tapestry5.services.MarkupRendererFilter}s,
 * which are used for ordinary page rendering (as opposed to {@linkplain org.apache.tapestry5.services.PartialMarkupRenderer
 * partial page rendering} for Ajax requests). The MarkupRenderer service may be selected using the
 *
 * @{@link org.apache.tapestry5.ioc.annotations.Primary} marker annotation.
 */
@UsesOrderedConfiguration(MarkupRendererFilter.class)
public interface MarkupRenderer
{
    /**
     * Invoked to render some markup.
     *
     * @param writer to which markup should be written
     */
    void renderMarkup(MarkupWriter writer);
}
