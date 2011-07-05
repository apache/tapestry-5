// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.services.dynamic;

import org.apache.tapestry5.corelib.components.Dynamic;
import org.apache.tapestry5.runtime.RenderCommand;

/**
 * A dynamic template is used by the {@link Dynamic} component to allow
 * a component to significantly alter its presentation at runtime. An external
 * template file provides most of the rendered content.
 * 
 * @since 5.3
 * @see DynamicTemplateParser
 */
public interface DynamicTemplate
{
    /**
     * Given a delegate (to assist with locating Blocks and evaluating expressions) ... create a
     * RenderCommand that can be returned from a render phase method.
     */
    RenderCommand createRenderCommand(DynamicDelegate delegate);
}
