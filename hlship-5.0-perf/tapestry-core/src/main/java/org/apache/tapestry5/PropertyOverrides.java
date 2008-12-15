//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5;

import org.apache.tapestry5.ioc.Messages;

/**
 * Provides access to blocks and messages used when overriding property labels, and property display or edit blocks.
 * Generally, this is a wrapper around {@link org.apache.tapestry5.ComponentResources}. An explicit implementation of
 * this could be used to, for example, search for override blocks in multiple places.
 *
 * @see org.apache.tapestry5.corelib.components.PropertyDisplay
 * @see org.apache.tapestry5.corelib.components.PropertyEditor
 */
public interface PropertyOverrides
{
    /**
     * Returns the override messages (normally, the messages catalog for the component's container).
     */
    Messages getOverrideMessages();

    /**
     * Searches for an override block with the given name.
     *
     * @param name the name of the block (typically, an informal parameter to a component)
     * @return the block if found, or null if not found
     */
    Block getOverrideBlock(String name);
}
