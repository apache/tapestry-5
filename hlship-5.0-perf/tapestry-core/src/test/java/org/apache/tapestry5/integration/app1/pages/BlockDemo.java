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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Retain;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.Map;

public class BlockDemo
{
    @Inject
    private Block fred;

    @Inject
    private Block barney;

    // Blocks not injected until page load, so must lazily initialize the map.
    @Retain
    private Map<String, Block> blocks = null;

    @Persist
    private String blockName;

    public Block getBlockToRender()
    {
        if (blocks == null)
        {
            blocks = CollectionFactory.newMap();
            blocks.put("fred", fred);
            blocks.put("barney", barney);
        }

        return blocks.get(blockName);
    }

    public String getBlockName()
    {
        return blockName;
    }

    public void setBlockName(String blockName)
    {
        this.blockName = blockName;
    }

}
