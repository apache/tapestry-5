// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.RenderCommand;
import org.apache.tapestry.services.ComponentEventResultProcessor;

import java.io.IOException;

/**
 * Processor for objects that implement {@link RenderCommand} (such as {@link org.apache.tapestry.internal.structure.BlockImpl}).
 *
 * @see AjaxPartialResponseRenderer#renderPartialPageMarkup(org.apache.tapestry.runtime.RenderCommand)
 */
public class RenderCommandComponentEventResultProcessor implements ComponentEventResultProcessor<RenderCommand>
{
    private final AjaxPartialResponseRenderer _renderer;

    public RenderCommandComponentEventResultProcessor(AjaxPartialResponseRenderer renderer)
    {
        _renderer = renderer;
    }

    public void processComponentEvent(RenderCommand value, Component component, String methodDescripion)
            throws IOException
    {
        _renderer.renderPartialPageMarkup(value);
    }
}
