//  Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Provides some assistance in determining <em>where</em> to place a hidden field based on standard (X)HTML elements.
 * <p/>
 * The service works based on a mapped service contribution; keys are the element names, values area {@link
 * org.apache.tapestry5.services.RelativeElementPosition}.
 */
@UsesMappedConfiguration(RelativeElementPosition.class)
public interface HiddenFieldLocationRules
{
    /**
     * Checks the element to see if a hidden field may be placed inside the element.
     */
    boolean placeHiddenFieldInside(Element element);

    /**
     * Checks the element to see if a hidden field may be placed after the element (as a sibling element).
     */
    boolean placeHiddenFieldAfter(Element element);
}
