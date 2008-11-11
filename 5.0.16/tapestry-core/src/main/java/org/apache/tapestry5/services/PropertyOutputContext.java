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

package org.apache.tapestry5.services;

import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.ioc.Messages;

/**
 * Provides context information needed when displaying a value. This interface is an integral part of the {@link Grid}
 * and similar output components.    It is made available to components via an {@link
 * org.apache.tapestry5.annotations.Environmental} annotation.
 */
public interface PropertyOutputContext
{
    /**
     * Returns the value of the property (the object being displayed is encapsulated by the context).
     */
    Object getPropertyValue();

    /**
     * Returns the message catalog appropriate for use. In practice, this is the message catalog of the container of the
     * {@link Grid} component. This is used, for example, to locate labels for fields, or to locate string
     * representations of Enums.
     */
    Messages getMessages();

    /**
     * Returns a string that identifies the property, usually the property name. This is used as the basis for the
     * client-side client id.
     */
    String getPropertyId();

    /**
     * Returns the name of the property (which may, in fact, be a property expression).
     */
    String getPropertyName();
}
