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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.ContextValueEncoder;
import org.slf4j.Logger;

/**
 * Provides access to common methods of various services, needed by implementations of {@link ComponentPageElement} and
 * {@link org.apache.tapestry5.internal.InternalComponentResources}.
 */
public interface ComponentPageElementResources extends ContextValueEncoder
{
    /**
     * Used to obtain a {@link org.apache.tapestry5.ioc.Messages} instance for a particular component. If the component
     * extends from another component, then its localized properties will merge with its parent's properties (with the
     * subclass overriding the super class on any conflicts).
     *
     * @param componentModel
     * @return the message catalog for the component, in the indicated locale
     * @see org.apache.tapestry5.internal.services.ComponentMessagesSource
     */
    Messages getMessages(ComponentModel componentModel);

    /**
     * Performs a coercion from an input type to a desired output type. When the target type is a primitive, the actual
     * conversion will be to the equivalent wrapper type. In some cases, the TypeCoercer will need to search for an
     * appropriate coercion, and may even combine existing coercions to form new ones; in those cases, the results of
     * the search are cached.
     *
     * @param <S>        source type (input)
     * @param <T>        target type (output)
     * @param input
     * @param targetType defines the target type
     * @return the coerced value
     * @see org.apache.tapestry5.ioc.services.TypeCoercer
     */
    <S, T> T coerce(S input, Class<T> targetType);

    /**
     * Gets the Class instance for then give name.
     *
     * @param className fully qualified class name
     * @return the class instance
     * @see org.apache.tapestry5.internal.services.ComponentClassCache
     */
    Class toClass(String className);

    /**
     * Creates a link on behalf of a component.
     *
     * @param resources resources for the component
     * @param eventType type of event to create
     * @param forForm   true if generating for a form submission
     * @param context   additional event context associated with the link
     * @return the link
     * @since 5.1.0.0
     */
    Link createComponentEventLink(ComponentResources resources, String eventType, boolean forForm, Object... context);

    /**
     * Creates a page render request link to render a specific page.
     *
     * @param pageName the logical name of the page to link to
     * @param override if true, the context is used even if empty (normally, the target page is allowed to passivate,
     *                 providing a context, when the provided context is empty)
     * @param context  the activation context for the page. If omitted, the activation context is obtained from the
     *                 target page
     * @return link for a render request to the targetted page
     * @since 5.1.0.0
     */
    Link createPageRenderLink(String pageName, boolean override, Object... context);

    /**
     * Creates a page render request link to render a specific page. Using a page class, rather than a page name, is
     * more refactoring safe (in the even the page is renamed or moved).
     *
     * @param pageClass identifies the page to link to
     * @param override  if true, the context is used even if empty (normally, the target page is allowed to passivate,
     *                  providing a context, when the provided context is empty)
     * @param context   the activation context for the page. If omitted, the activation context is obtained from the
     *                  target page
     * @return link for a render request to the targetted page
     * @since 5.1
     */
    Link createPageRenderLink(Class pageClass, boolean override, Object... context);

    /**
     * Returns the event logger for the provided component logger.  The event logger is based on the component logger's
     * name (which matches the component class name) with a "tapestry..events." prefix.
     *
     * @param componentLogger provides base name for logger
     * @return the logger
     */
    Logger getEventLogger(Logger componentLogger);
}
