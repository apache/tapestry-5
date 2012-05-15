// Copyright 2010, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.annotations.PageReset;

/**
 * Optional interface implemented to support resetting of the page state.
 * 
 * @since 5.2.0
 * @see PageReset
 * @deprecated in 5.3.4
 * @see org.apache.tapestry5.runtime.PageLifecycleCallbackHub#addResetCallback(Runnable)
 */
public interface PageResetListener
{
    /**
     * Invoked when the page is accessed from some other page. This notification will be sent
     * <em>after</em> the page
     * has been activated.
     */
    void containingPageDidReset();
}
