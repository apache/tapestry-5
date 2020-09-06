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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleCallbackHub;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.slf4j.Logger;

/**
 * Represents a unique page within the application. Pages are part of the <em>internal</em> structure of a Tapestry
 * application; end developers who refer to "page" are really referring to the {@link #getRootComponent() root
 * component} of the actual page.
 *
 * Starting in release 5.2, the nature of pages changed considerably. Pages are no longer pooled instances. Instead,
 * pages are shared instances (per locale) but all internal <em>mutable</em> state is stored inside
 * {@link PerthreadManager}. Page construction time is considered to extend past the
 * {@linkplain  PageLifecycleCallbackHub#addPageLoadedCallback(Runnable) page loaded callback}. This is not quite perfect from a
 * threading point-of-view (arguably, even write-once-read-many fields should be protected by synchronized blocks or
 * other mechanisms). At best, we can be assured that the entire page construction phase is protected by a single
 * synchronized block (but not on the page itself). An ideal system would build the page bottom to top so that all
 * assignments could take place in constructors, assigning to final fields. Maybe some day.
 *
 * The Page object is never visible to end-user code, though it exposes an interface ({@link PageLifecycleCallbackHub} that
 * {@linkplain org.apache.tapestry5.ComponentResources#getPageLifecycleCallbackHub() is}).
 */
public interface Page extends PageLifecycleCallbackHub
{
    /**
     * Page construction statistics for the page.
     *
     * @since 5.3
     */
    public final class Stats
    {
        /**
         * Time, in milliseconds, to construct the page. This includes time to construct components inside the page,
         * as well as hooking everything together, and includes the execution of {@link org.apache.tapestry5.internal.structure.Page#loaded()}.
         * You'll often see that the first page is expensive to construct,
         * and later pages that use a similar mix of components are very cheap.
         */
        public final double assemblyTime;

        /**
         * The total number of components in the page, including the root component. This does not include the number of mixins.
         */
        public final int componentCount;

        /**
         * The "weight" of the page is an arbitrary number that factors the number of components, mixins, component template elements,
         * bindings, and other factors.
         */
        public final int weight;

        public Stats(double assemblyTime, int componentCount, int weight)
        {
            this.assemblyTime = assemblyTime;
            this.componentCount = componentCount;
            this.weight = weight;
        }
    }

    /**
     * Returns the short, logical name for the page. This is the page name as it might included in
     * an action or page
     * render URL (though it will be converted to lower case when it is included).
     */
    String getName();

    /**
     * The selector (which includes Locale) used when the page was constructor.
     */
    ComponentResourceSelector getSelector();

    /**
     * Invoked during page construction time to connect the page's root component to the page
     * instance.
     */
    void setRootElement(ComponentPageElement component);

    /**
     * The root component of the page. This is the wrapper around the end developer's view of the
     * page.
     */
    ComponentPageElement getRootElement();

    /**
     * The root component of the page. A convenience over invoking getRootElement().getComponent().
     */
    Component getRootComponent();

    /**
     * Invoked to inform the page that it is being detached from the current request. This occurs
     * just before the page
     * is returned to the page pool.
     *
     * A page may be clean or dirty. A page is dirty if its dirty count is greater than zero (meaning that, during the
     * render of the page, some components did not fully render), or if any of its listeners throw an exception from
     * containingPageDidDetach().
     *
     * The page pool should discard pages that are dirty, rather than store them into the pool.
     *
     * Under Tapestry 5.2 and pool-less pages, the result is ignored; all mutable state is expected to be discarded
     * automatically from the {@link PerthreadManager}. A future release of Tapestry will likely convert this method to
     * type void.
     *
     * @return true if the page is "dirty", false otherwise
     * @see org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidDetach()
     */
    boolean detached();

    /**
     * Invoked to inform the page that it is attached to the current request. This occurs when a
     * page is first referenced within a request. If the page was created from scratch for this request, the call
     * to {@link #loaded()} will preceded the call to {@link #attached()}.
     *
     * First all listeners have {@link PageLifecycleListener#restoreStateBeforePageAttach()} invoked, followed by
     * {@link PageLifecycleListener#containingPageDidAttach()}.
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
     *
     * @deprecated in 5.3.4; use {@link #addPageLoadedCallback(Runnable)}, {@link #addPageAttachedCallback(Runnable)}, or
     * {@link #addPageDetachedCallback(Runnable)}  instead
     */
    void addLifecycleListener(PageLifecycleListener listener);

    /**
     * Removes a listener that was previously added.
     *
     * @since 5.2.0
     * @deprecated in 5.3.4, due to introduction of {@link #addPageLoadedCallback(Runnable)}
     */
    void removeLifecycleListener(PageLifecycleListener listener);

    /**
     * Returns the logger of the root component element. Any logging about page construction or
     * activity should be sent
     * to this logger.
     */
    Logger getLogger();

    /**
     * Retrieves a component element by its nested id (a sequence of simple ids, separated by dots).
     * The individual
     * names in the nested id are matched without regards to case. A nested id of '' (the empty
     * string) returns the root
     * element of the page.
     *
     * @throws UnknownValueException
     *         if the nestedId does not correspond to a component
     */
    ComponentPageElement getComponentElementByNestedId(String nestedId);

    /**
     * Posts a change to a persistent field.
     *
     * @param resources
     *         the component resources for the component or mixin containing the field whose
     *         value changed
     * @param fieldName
     *         the name of the field
     * @param newValue
     *         the new value for the field
     */
    void persistFieldChange(ComponentResources resources, String fieldName, Object newValue);

    /**
     * Gets a change for a field within the component.
     *
     * @param nestedId
     *         the nested component id of the component containing the field
     * @param fieldName
     *         the name of the persistent field
     * @return the value, or null if no value is stored
     */
    Object getFieldChange(String nestedId, String fieldName);

    /**
     * Discards all persistent field changes for the page containing the component. Changes are
     * eliminated from
     * persistent storage (such as the {@link org.apache.tapestry5.http.services.Session}) which will
     * take effect in the <em>next</em> request (the attached page instance is not affected).
     */
    void discardPersistentFieldChanges();

    /**
     * Adds a new listener for page reset events.
     *
     * @param listener
     *         will receive notifications when the page is accessed from a different page
     * @since 5.2.0
     * @deprecated in 5.3.4,
     */
    void addResetListener(PageResetListener listener);

    /**
     * Returns true if there are any {@link PageResetListener} listeners.
     *
     * @since 5.2.0
     */
    boolean hasResetListeners();

    /**
     * Invoked to notify {@link PageResetListener} listeners.
     */
    void pageReset();

    /**
     * Invoked once at the end of page construction, to provide page construction statistics.
     *
     * @since 5.3
     */
    void setStats(Stats stats);

    /**
     * Returns the page construction statistics for this page.
     */
    Stats getStats();

    /**
     * Returns the number of times the page has been attached to a request. This is a rough measure
     * of how important the page is, relative to other pages. This value is volatile, changing constantly.
     *
     * @since 5.3
     */
    int getAttachCount();

    /**
     * Returns true if extract parameter count matching is enabled.
     *
     * @see org.apache.tapestry5.MetaDataConstants#UNKNOWN_ACTIVATION_CONTEXT_CHECK
     * @since 5.4
     */
    @NonVisual
    boolean isExactParameterCountMatch();


}
