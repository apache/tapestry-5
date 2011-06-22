//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5;

/**
 * <em>Optional</em> interface that may be implemented by an Application State Object.
 *
 * @see org.apache.tapestry5.annotations.ApplicationState
 * @see org.apache.tapestry5.services.ApplicationStateManager
 * @deprecated since 5.1.0.0; use {@link org.apache.tapestry5.OptimizedSessionPersistedObject} instead
 */
public interface OptimizedApplicationStateObject
{
    /**
     * Determines if the application state object has changed its state since being read from the session.
     *
     * @return true if the ASO has changed and needs resaving, false otherwise
     */
    boolean isApplicationStateObjectDirty();
}
