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

import org.apache.tapestry.corelib.components.Grid;
import org.apache.tapestry.corelib.components.GridCell;
import org.apache.tapestry.ioc.Messages;

/**
 * Provides context information needed when displaying a value in the context of a {@link Grid}
 * component (or, really, the {@link GridCell} component).
 */
public interface PropertyDisplayContext
{
    /**
     * Returns the value of the property (the object being displayed is encapsulated by the
     * context).
     */
    Object getPropertyValue();

    /**
     * Returns the message catalog appropriate for use. In practice, this is the message catalog of
     * the container of the {@link Grid} component.
     */
    Messages getContainerMessages();
}
