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

package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.Block;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.annotations.Retain;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;

import java.util.Map;

public class BlockDemo
{
    @Inject
    private Block _fred;

    @Inject
    private Block _barney;

    // Blocks not injected until page load, so must lazily initialize the map.
    @Retain
    private Map<String, Block> _blocks = null;

    @Persist
    private String _blockName;

    public Block getBlockToRender()
    {
        if (_blocks == null)
        {
            _blocks = CollectionFactory.newMap();
            _blocks.put("fred", _fred);
            _blocks.put("barney", _barney);
        }

        return _blocks.get(_blockName);
    }

    public String getBlockName()
    {
        return _blockName;
    }

    public void setBlockName(String blockName)
    {
        _blockName = blockName;
    }

}
