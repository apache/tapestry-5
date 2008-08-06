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

package org.apache.tapestry5.internal.event;

import org.apache.tapestry5.internal.events.InvalidationListener;

/**
 * An object which manages a list of {@link org.apache.tapestry5.internal.events.InvalidationListener}s.
 * <p/>
 * TODO: This interface need to move to the public side (as it is extended by other public interfaces), or we need to
 * come up with an alternate mechanism for propogating invalidation data.
 */
public interface InvalidationEventHub
{
    void addInvalidationListener(InvalidationListener listener);
}
