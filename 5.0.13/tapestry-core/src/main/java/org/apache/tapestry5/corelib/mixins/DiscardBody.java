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

package org.apache.tapestry5.corelib.mixins;

import org.apache.tapestry5.annotations.BeforeRenderBody;
import org.apache.tapestry5.annotations.MixinAfter;

/**
 * Discards a component's body. Returns false from the {@link BeforeRenderBody} phase, which prevents the rendering of
 * the body. Set up as a "MixinAfter" so that components can render their an alternative body if they so desire before
 * this mixin cancels the normal body (from the container's template).
 */
@MixinAfter
public class DiscardBody
{
    boolean beforeRenderBody()
    {
        return false;
    }
}
