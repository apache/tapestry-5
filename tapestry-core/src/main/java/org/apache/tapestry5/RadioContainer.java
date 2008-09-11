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

package org.apache.tapestry5;

import org.apache.tapestry5.corelib.components.Radio;

/**
 * A container of {@link Radio} components, used to identify the element name used when rendering the individual radio
 * buttons (all buttons in a group share the same element name) and to
 */
public interface RadioContainer
{
    /**
     * Returns the value used as the name attribute of the rendered element. This value will be unique within an
     * enclosing form, even if the same component renders multiple times.
     *
     * @see org.apache.tapestry5.Field#getControlName()
     */
    String getControlName();

    /**
     * If true, then all buttons within the container should also be disabled.
     */
    boolean isDisabled();

    /**
     * Converts an object to a client-side string representation of that value.
     *
     * @param value to convert (may be null)
     * @return string representation of the value
     * @see ValueEncoder#toClient(Object)
     */
    String toClient(Object value);

    /**
     * Returns true if the value is the current selected value.
     */
    boolean isSelected(Object value);
}
