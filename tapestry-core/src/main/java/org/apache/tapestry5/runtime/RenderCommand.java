// Copyright 2006 The Apache Software Foundation
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

import org.apache.tapestry5.MarkupWriter;

/**
 * A command used during rendering of a page.
 */
public interface RenderCommand
{
    /**
     * Invoked on an object to request that it render itself. This involves a mix of invoking methods on the writer, and
     * queueing up additional commands (often, representing children of the object that was invoked) to perform
     * additional rendering.
     * <p/>
     * In this way, rendering is a tail recursive algorithm, but is not implemented using tail recursion.
     */
    void render(MarkupWriter writer, RenderQueue queue);
}
