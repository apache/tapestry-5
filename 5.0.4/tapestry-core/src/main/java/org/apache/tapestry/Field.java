// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry;

/**
 * Defines a field within a form.
 */
public interface Field extends ClientElement
{
    /**
     * Returns the value used as the name attribute of the rendered element. This value will be
     * unique within an enclosing form, even if the same component renders multiple times.
     */
    String getElementName();

    /**
     * Returns a user presentable (localized) label for the field, which may be used inside
     * &lt;label&gt; elements on the client, and inside client or server-side validation error
     * messages.
     * 
     * @return the label
     */
    String getLabel();

    /**
     * Returns true if the field is disabled; A disabled field will render a disabled attribute so
     * that it is non-responsive on the client (at least, until its disabled status is changed on
     * the client using JavaScript). A disabled field will ignore any value passed up in a form
     * submit request. Care must be taken if the disabled status of a field can change between the
     * time the field is rendered and the time the enclosing form is submitted.
     */
    boolean isDisabled();
}
