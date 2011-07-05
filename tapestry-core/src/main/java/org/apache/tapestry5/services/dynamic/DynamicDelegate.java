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

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.corelib.components.Dynamic;

/**
 * Used by implementations of {@link DynamicTemplate} to obtain {@link Block}s as replacements
 * for elements within the template. The Blocks are passed to the {@link Dynamic} component as informal parameters.
 * 
 * @since 5.3
 */
public interface DynamicDelegate
{
    /**
     * Returns the component resources (i.e., the {@link Dynamic} component), used when creating bindings for expansions
     * located inside the dynamic template.
     */
    ComponentResources getComponentResources();

    /**
     * Returns the Block with the given unique name.
     * 
     * @throws RuntimeException
     *             if no such block exists
     */
    Block getBlock(String name);
}
