// Copyright 2006 The Apache Software Foundation
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

import java.util.Locale;

import org.apache.tapestry.internal.event.InvalidationEventHub;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.model.ComponentModel;

/** Used to connect a Tapestry component to its message catalog. */
public interface ComponentMessagesSource extends InvalidationEventHub
{
    /**
     * Used to obtain a {@link Messages} instance for a particular component, within a particular
     * locale. If the component extends from another component, then its localized properties will
     * merge with its parent's properties (with the subclass overriding the super class on any
     * conflicts).
     * 
     * @param componentModel
     * @param locale
     * @return the message catalog for the component, in the indicated locale
     */
    Messages getMessages(ComponentModel componentModel, Locale locale);
}
