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
 * Base class for creating optimized application state objects.  Works as a {@link
 * javax.servlet.http.HttpSessionBindingListener} to determine when the object is no longer dirty.
 *
 * @deprecated since 5.1.0.0; use {@link org.apache.tapestry5.BaseOptimizedSessionPersistedObject} instead
 */
public abstract class BaseOptimizedApplicationStateObject extends BaseOptimizedSessionPersistedObject
{
    public final boolean isApplicationStateObjectDirty()
    {
        return isSessionPersistedObjectDirty();
    }
}
