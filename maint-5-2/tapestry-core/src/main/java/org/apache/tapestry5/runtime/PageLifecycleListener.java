// Copyright 2006, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.runtime;

/**
 * A set of methods that allow components to know about page-level operations.
 */
public interface PageLifecycleListener
{
    /**
     * Invoked when the page finishes loading. This occurs once all components are loaded and all parameters have been
     * set.
     */
    void containingPageDidLoad();

    /**
     * Invoked when the page is detached, allowing components a chance to clear out any temporary or client specific
     * state.
     */
    void containingPageDidDetach();

    /**
     * Invoked when a page is first attached to the current request, giving components a chance to initialize for the
     * current request.
     */
    void containingPageDidAttach();

    /**
     * A kind of "pre-attach" phase allowing components to restore internal state before handling the actual attach;
     * this is primarily used to restore persisted fields.
     *
     * @since 5.1.0.1
     */
    void restoreStateBeforePageAttach();
}
