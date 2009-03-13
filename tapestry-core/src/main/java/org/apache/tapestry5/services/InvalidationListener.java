// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.services;

/**
 * Interface for objects that may cache information that can be invalidated. Invalidation occurs when external files,
 * from which in-memory data is cached, is determined to have changed. Granularity is very limited; when any external
 * file is found to have changed, the event is fired (with the expectation that the cleared cache will be repopulated as
 * necessary).
 *
 * @see org.apache.tapestry5.services.InvalidationEventHub
 * @since 5.1.0.0
 */
public interface InvalidationListener
{
    /**
     * Invoked to indicate that some object is invalid. The receiver should clear its cache.
     */
    void objectWasInvalidated();
}
