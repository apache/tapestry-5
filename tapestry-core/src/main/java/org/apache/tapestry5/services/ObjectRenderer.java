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
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * A strategy interface used for converting an object into markup that describes that object. This is primarily used in
 * terms of an {@link org.apache.tapestry5.services.ExceptionReporter} page.
 * <p/>
 * The ObjectRenderer service (distinguished by the @{@link org.apache.tapestry5.ioc.annotations.Primary} marker
 * annotation) uses {@linkplain org.apache.tapestry5.ioc.services.StrategyBuilder type-based matching} to find a
 * specific ObjectRenderer for any given type.
 */
@UsesMappedConfiguration(key = Class.class, value = ObjectRenderer.class)
public interface ObjectRenderer<T>
{
    /**
     * Renders the object out as markup.
     *
     * @param object to be rendered
     * @param writer to which output should be directed
     */
    void render(T object, MarkupWriter writer);
}
