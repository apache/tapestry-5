// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.runtime;

/**
 * Defines a way for different aspects of a page to add callbacks for important lifecycle events.
 *
 * @see org.apache.tapestry5.ComponentResources#getPageLifecycleCallbackHub()
 * @since 5.3.4
 */
public interface PageLifecycleCallbackHub
{
    /**
     * Adds a callback for when the page is first loaded.  Callbacks are invoked in the order they
     * are added to the page. They are invoked once and then discarded.
     *
     * @param callback
     *         invoked once, when page is first loaded
     * @since 5.3.4
     */
    void addPageLoadedCallback(Runnable callback);

    /**
     * Adds a callback for when the page is attached to the request.
     *
     * @param callback
     * @since 5.3.4
     */
    void addPageAttachedCallback(Runnable callback);

    /**
     * Adds a callback for when the page is detached from the request.
     *
     * @param callback
     * @since 5.3.4
     */
    void addPageDetachedCallback(Runnable callback);

    /**
     * Adds a verify callback, which is allowed while the page is loading. Such callbacks are invoked once,
     * after the page has been loaded successfully, and are then discarded. This was added specifically to ensure that components
     * only verify that required parameters are bound after all components and mixins of the page have had a chance
     * to initialize.
     *
     * @param callback
     *         to be invoked after page loaded
     * @since 5.3
     */
    void addVerifyCallback(Runnable callback);

    /**
     * A reset occurs when a request for a page arrives that did not originate on the same page. This gives the application a chance to reset the state of the page.
     *
     * @param callback
     *         invoked when a page is activated by link from some other page.
     */
    void addResetCallback(Runnable callback);
}
