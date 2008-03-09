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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Block;
import org.apache.tapestry.services.BeanBlockContribution;
import org.apache.tapestry.services.BeanBlockOverrideSource;
import org.apache.tapestry.services.BeanBlockSource;

import java.util.Collection;

public class BeanBlockSourceImpl implements BeanBlockSource
{
    // This is checked before _masterSource

    private final BeanBlockOverrideSource _overrideSource;

    private final BeanBlockOverrideSource _masterSource;


    public BeanBlockSourceImpl(RequestPageCache pageCache,
                               BeanBlockOverrideSource overrideSource, Collection<BeanBlockContribution> configuration)
    {
        _overrideSource = overrideSource;
        _masterSource = new BeanBlockOverrideSourceImpl(pageCache, configuration);
    }

    public boolean hasDisplayBlock(String datatype)
    {
        return _overrideSource.hasDisplayBlock(datatype) || _masterSource.hasDisplayBlock(datatype);
    }

    public Block getDisplayBlock(String datatype)
    {
        Block result = _overrideSource.getDisplayBlock(datatype);

        if (result == null)
            result = _masterSource.getDisplayBlock(datatype);

        if (result == null)
            throw new RuntimeException(ServicesMessages.noDisplayForDataType(datatype));

        return result;
    }

    public Block getEditBlock(String datatype)
    {
        Block result = _overrideSource.getEditBlock(datatype);

        if (result == null)
            result = _masterSource.getEditBlock(datatype);

        if (result == null)
            throw new RuntimeException(ServicesMessages.noEditForDataType(datatype));

        return result;
    }

}
