// Copyright 2006, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.model;

import java.util.List;

/**
 * A mutable version of {@link org.apache.tapestry5.model.EmbeddedComponentModel} that allows the parameters to be
 * incrementally stored.
 */
public interface MutableEmbeddedComponentModel extends EmbeddedComponentModel
{
    void addParameter(String name, String value);

    /**
     * Adds a mixin to the component in terms of its fully qualified class name.
     */
    void addMixin(String mixinClassName);

    /**
     * Sets the list of published parameters for this embedded component.
     *
     * @param parameterNames list of names
     * @see org.apache.tapestry5.annotations.Component#publishParameters()
     * @since 5.1.0.0
     */
    void setPublishedParameters(List<String> parameterNames);
}
