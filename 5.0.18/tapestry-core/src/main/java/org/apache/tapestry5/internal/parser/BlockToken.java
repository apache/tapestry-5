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

package org.apache.tapestry5.internal.parser;

import org.apache.tapestry5.ioc.Location;

/**
 * A block, used to enclose a chunk of template (including components) and control when or if the content is rendered.
 */
public class BlockToken extends TemplateToken
{
    private final String id;

    /**
     * @param id       the id of the block, or null for an anonymous block
     * @param location of the block element
     */
    public BlockToken(String id, Location location)
    {
        super(TokenType.BLOCK, location);

        this.id = id;
    }

    /**
     * Returns the block's template-unique id, or null if the block element did not specify an id.
     */
    public String getId()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return String.format("Block[%s]", id == null ? "" : id);
    }
}
