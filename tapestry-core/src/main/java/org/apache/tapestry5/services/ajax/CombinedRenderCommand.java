// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.services.ajax;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;

/**
 * Combines exactly two render commands by pushing each onto the render queue.
 *
 * @since 5.1.0.1
 */
public class CombinedRenderCommand implements RenderCommand
{
    private final RenderCommand first;

    private final RenderCommand second;

    public CombinedRenderCommand(RenderCommand first, RenderCommand second)
    {
        this.first = first;
        this.second = second;
    }

    public void render(MarkupWriter writer, RenderQueue queue)
    {
        queue.push(second);

        queue.push(first);
    }
}
