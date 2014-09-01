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

package org.apache.tapestry5.runtime;

/**
 * A set of methods that allow components to know about page-level operations. When this interface is
 * {@linkplain org.apache.tapestry5.plastic.PlasticClass#introduceInterface(Class)} introduced}, the component will
 * automatically register itself as a listener with the page.
 *
 * @deprecated in 5.3.4, replaced with {@link PageLifecycleCallbackHub}
 */
public interface PageLifecycleListener
{
    /**
     * Invoked when the page finishes loading. This occurs once all components are loaded and all parameters have been
     * set.
     *
     * @deprecated in 5.3.4,  use {@link org.apache.tapestry5.runtime.PageLifecycleCallbackHub#addPageLoadedCallback(Runnable)} instead
     */
    void containingPageDidLoad();

    /**
     * Invoked when the page is detached, allowing components a chance to clear out any temporary or client specific
     * state.
     *
     * @deprecated In Tapestry 5.3, use {@link PageLifecycleCallbackHub#addPageDetachedCallback(Runnable)} instead.
     */
    void containingPageDidDetach();

    /**
     * Invoked when a page is first attached to the current request, giving components a chance to initialize for the
     * current request.
     *
     * @deprecated In Tapestry 5.3, use {@link org.apache.tapestry5.runtime.PageLifecycleCallbackHub#addPageAttachedCallback(Runnable)} instead.
     */
    void containingPageDidAttach();

    /**
     * A kind of "pre-attach" phase allowing components to restore internal state before handling the actual attach;
     * this is primarily used to restore persisted fields.
     *
     * @since 5.1.0.1
     * @deprecated In Tapestry 5.3, with no replacement (persisted fields now lazily restore their state)
     */
    void restoreStateBeforePageAttach();
}
