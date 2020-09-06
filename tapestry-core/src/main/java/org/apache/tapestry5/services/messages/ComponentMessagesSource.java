// Copyright 2006, 2008, 2009, 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.services.messages;

import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.ioc.annotations.NotLazy;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

import java.util.Locale;

/**
 * Used to connect a Tapestry component to its message catalog, or to obtain the application catalog (that all
 * component message catalogs extend from). The application catalog is defined by the collection of {@link Resource}s
 * contributed to the service. In general, component libraries will contribute a Resource before the "AppCatalog"
 * resource (representing
 * the application message catalog, WEB-INF/app.properties) so that the application can override messages of the
 * component library.
 */
@UsesOrderedConfiguration(Resource.class)
public interface ComponentMessagesSource
{
    /**
     * Used to obtain a {@link Messages} instance for a particular component, within a particular locale. If the
     * component extends from another component, then its localized properties will merge with its parent's properties
     * (with the subclass overriding the super class on any conflicts).
     *
     * @param componentModel
     * @param locale
     * @return the message catalog for the component, in the indicated locale
     * @deprecated Deprecated in 5.3; use {@link #getMessages(ComponentModel, ComponentResourceSelector)} instead.
     */
    Messages getMessages(ComponentModel componentModel, Locale locale);

    /**
     * Used to obtain a {@link Messages} instance for a particular component, using a particular selector. If the
     * component extends from another component, then its localized properties will merge with its parent's properties
     * (with the subclass overriding the super class on any conflicts).
     *
     * @param componentModel
     * @param selector       determined correct version of messages to obtain
     * @return the message catalog for the component, in the indicated selector
     * @since 5.3
     */
    Messages getMessages(ComponentModel componentModel, ComponentResourceSelector selector);

    /**
     * Gets the Messages derived from the application's message catalog.
     *
     * @since 5.2.0
     */
    Messages getApplicationCatalog(Locale locale);

    /**
     * Returns the event hub that allows listeners to be notified when any underlying message catalog file is changed.
     *
     * @since 5.1.0.0
     */
    @NotLazy
    InvalidationEventHub getInvalidationEventHub();
}
