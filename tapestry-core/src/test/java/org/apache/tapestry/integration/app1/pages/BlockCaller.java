// Copyright 2008 The Apache Software Foundation
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
import org.apache.tapestry.annotations.InjectPage;

/**
 * Part of testing for TAPESTRY-2044
 */
public class BlockCaller
{
    @InjectPage
    private BlockHolder _blockHolder;

    private int _activationContext;

    public void setActivationContext(int value)
    {
        _activationContext = value;
    }

    public int getActivationContext()
    {
        return _activationContext;
    }

    void onActivate(int activationContext)
    {
        _activationContext = activationContext;
    }

    int onPassivate()
    {
        return _activationContext;
    }

    public Block getBlock()
    {
        return _blockHolder.getLinks();
    }
}
