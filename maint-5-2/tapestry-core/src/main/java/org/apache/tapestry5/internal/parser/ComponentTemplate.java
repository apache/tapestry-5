// Copyright 2006, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.parser;

import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;

import java.util.List;
import java.util.Map;

/**
 * A parsed component template, containing all the tokens parsed from the template.
 */
public interface ComponentTemplate
{
    /**
     * Returns true if no template could be found for the component.
     */
    boolean isMissing();

    /**
     * Returns true if this component template is an extension of its parent class' template.
     *
     * @since 5.1.0.1
     */
    boolean isExtension();

    /**
     * Returns a list of tokens associated with an extension point, or null if this template neither defines the
     * extension point nor overrides it.
     *
     * @param extensionPointId
     * @return list of tokens provided in this template, or null
     * @since 5.1.0.1
     */
    List<TemplateToken> getExtensionPointTokens(String extensionPointId);

    /**
     * Returns the resource that was parsed to form the template.
     */
    Resource getResource();

    /**
     * Returns a list of tokens that were parsed from the template. The caller should not modify this list.
     */
    List<TemplateToken> getTokens();

    /**
     * Identifies     {@link org.apache.tapestry5.internal.parser.StartComponentToken}s with a non-blank id, mapping the
     * id to its location (within the template). This is used to report unmatched ids (where the component, or its
     * super-classes, do not define an embedded component).
     *
     * @see org.apache.tapestry5.annotations.Component (used to define an embedded component)
     */
    Map<String, Location> getComponentIds();
}
