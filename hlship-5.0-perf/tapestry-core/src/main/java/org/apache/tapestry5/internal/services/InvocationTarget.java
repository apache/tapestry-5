// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

/**
 * It represents target for a {@link org.apache.tapestry5.internal.services.ComponentInvocation}. For example, it may be
 * a page or an action for a component within a page.
 *
 * @see org.apache.tapestry5.services.Dispatcher
 */
public interface InvocationTarget
{
    /**
     * Represents the invocation as a path, part of a larger URI. The path will come after the context path and before
     * extra path info (converted from event or page activation context values).
     */
    String getPath();
}
