// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.Parameter;

/**
 * A component that does not do any rendering of its own, but will delegate to some other object that can do rendering.
 * This other object may be a component or a {@link Block} (among other things).
 */
public class Delegate
{
    /**
     * The object which will be rendered in place of the Delegate component. This is typically a specific component
     * instance, or a {@link Block}.
     */
    @Parameter(required = true)
    private Object to;

    Object beginRender()
    {
        return to;
    }
}
