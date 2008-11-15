// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.internal;

/**
 * Interface that allows an enclosing {@link org.apache.tapestry5.corelib.components.AjaxFormLoop} to work with enclosed
 * components such as {@link org.apache.tapestry5.corelib.components.AddRowLink} or {@link
 * org.apache.tapestry5.corelib.components.RemoveRowLink}.
 */
public interface AjaxFormLoopContext
{
    /**
     * Adds a clientId to the list of client-side elements that trigger the addition of a new row.
     *
     * @param clientId unique id (typically via {@link org.apache.tapestry5.RenderSupport#allocateClientId(org.apache.tapestry5.ComponentResources)})
     */
    void addAddRowTrigger(String clientId);

    /**
     * Used by {@link org.apache.tapestry5.corelib.components.RemoveRowLink} to
     *
     * @param clientId
     */
    void addRemoveRowTrigger(String clientId);
}
