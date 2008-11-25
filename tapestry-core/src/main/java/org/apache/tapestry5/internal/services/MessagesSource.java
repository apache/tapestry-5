// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.services.InvalidationEventHub;
import org.apache.tapestry5.services.UpdateListener;

import java.util.Locale;

public interface MessagesSource extends InvalidationEventHub, UpdateListener
{
    /**
     * Used to obtain a {@link Messages} instance for a particular component, within a particular locale. If the
     * component extends from another component, then its localized properties will merge with its parent's properties
     * (with the subclass overriding the super class on any conflicts).
     *
     * @param bundle defines the set of properties files to read, as well as a series of parent bundles to extend and
     *               override
     * @param locale
     * @return the message catalog for the bundle, in the indicated locale
     */
    Messages getMessages(MessagesBundle bundle, Locale locale);
}
