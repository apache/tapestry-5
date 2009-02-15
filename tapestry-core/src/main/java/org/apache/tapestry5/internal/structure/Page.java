// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.slf4j.Logger;

import java.util.Locale;

/**
 * Represents a unique page within the application. Pages are part of the <em>internal</em> structure of a Tapestry
 * application; end developers who refer to "page" are really referring to the {@link #getRootComponent() root
 * component} of the actual page.
 * <p/>
 * One of the most important aspects of a Page is that it <em>does not</em> have to be coded in a thread-safe manner.
 * Pages are always accessed within a single thread, associated with a single incoming request.
 * <p/>
 * The Page object is never visible to end-user code. The page also exists to provide a kind of service to components
 * embedded (directly or indirectly) within the page.
 */
public interface Page
{
    /**
     * Returns the short, logical name for the page. This is the page name as it might included in an action or page
     * render URL (though it will be converted to lower case when it is included).
     */
    String getName();

    /**
     * The locale for which the page is localized. This is set when the page is created and does not change.
     */
    Locale getLocale();

    /**
     * Invoked during page construction time to connect the page's root component to the page instance.
     */
    void setRootElement(ComponentPageElement component);

    /**
     * The root component of the page. This is the wrapper around the end developer's view of the page.
     */
    ComponentPageElement getRootElement();

    /**
     * The root component of the page. A convenience over invoking getRootElement().getComponent().
     */
    Component getRootComponent();

    /**
     * Invoked to inform the page that it is being detached from the current request. This occurs just before the page
     * is returned to the page pool.
     * <p/>
     * A page may be clean or dirty. A page is dirty if its dirty count is greater than zero (meaning that, during the
     * render of the page, some components did not fully render), or if any of its listeners throw an exception from
     * containingPageDidDetech().
     * <p/>
     * The page pool should discard pages that are dirty, rather than store them into the pool.
     *
     * @return true if the page is "dirty", false otherwise
     * @see org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidDetach()
     */
    boolean detached();

    /**
     * Invoked to inform the page that it is attached to the current request. This occurs when a page is first
     * referenced within a request. If the page was created from scratch for this request, the call to {@link #loaded()}
     * will preceded the call to {@link #attached()}.
     */

    void attached();

    /**
     * Inform the page that it is now completely loaded.
     *
     * @see org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidLoad()
     */

    void loaded();

    /**
     * Adds a listener that is notified of large scale page events.
     */
    void addLifecycleListener(PageLifecycleListener listener);

    /**
     * Returns the logger of the root component element. Any logging about page construction or activity should be sent
     * to this logger.
     */
    Logger getLogger();

    /**
     * Retrieves a component element by its nested id (a sequence of simple ids, separated by dots). The individual
     * names in the nested id are matched without regards to case. A nested id of '' (the empty string) returns the root
     * element of the page.
     *
     * @throws IllegalArgumentException if the nestedId does not correspond to a component
     */
    ComponentPageElement getComponentElementByNestedId(String nestedId);

    /**
     * Posts a change to a persistent field.
     *
     * @param resources the component resources for the component or mixin containing the field whose value changed
     * @param fieldName the name of the field
     * @param newValue  the new value for the field
     */
    void persistFieldChange(ComponentResources resources, String fieldName, Object newValue);

    /**
     * Gets a change for a field within the component.
     *
     * @param nestedId  the nested component id of the component containing the field
     * @param fieldName the name of the persistent field
     * @return the value, or null if no value is stored
     */
    Object getFieldChange(String nestedId, String fieldName);

    /**
     * Called as a component initially starts to render itself. This is used to check for the cases where a component
     * causes a runtime exception that aborts the render early, leaving the page in an invalid state.
     */
    void incrementDirtyCount();

    /**
     * Called as a component finishes rendering itself.
     */
    void decrementDirtyCount();

    /**
     * Discards all persistent field changes for the page containing the component.  Changes are eliminated from
     * persistent storage (such as the {@link org.apache.tapestry5.services.Session}) which will take effect in the
     * <em>next</em> request (the attached page instance is not affected).
     */
    void discardPersistentFieldChanges();
}
