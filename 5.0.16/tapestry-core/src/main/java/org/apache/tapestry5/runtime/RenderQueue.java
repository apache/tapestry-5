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

package org.apache.tapestry5.runtime;

import org.apache.tapestry5.ComponentResources;

/**
 * A stateful object that manages the process of rendering a page. Rending a page in Tapestry is based on a command
 * queue.
 */
public interface RenderQueue
{
    /**
     * Adds the new command to the front of the queue.
     */
    void push(RenderCommand command);

    /**
     * Indicates that a component is starting its render. A stack of active components is used for exception reporting.
     *
     * @param resources identifies the component that is rendering
     */
    void startComponent(ComponentResources resources);

    /**
     * Corresponds to {@link #startComponent(String)}, used to denote when the most recently started component finishes
     * rendering.
     */
    void endComponent();
}
