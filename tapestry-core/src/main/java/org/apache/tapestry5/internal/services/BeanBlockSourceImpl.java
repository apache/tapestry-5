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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.services.BeanBlockContribution;
import org.apache.tapestry5.services.BeanBlockOverrideSource;
import org.apache.tapestry5.services.BeanBlockSource;

import java.util.Collection;

public class BeanBlockSourceImpl implements BeanBlockSource
{
    // This is checked before masterSource
    private final BeanBlockOverrideSource overrideSource;

    private final BeanBlockOverrideSource masterSource;

    public BeanBlockSourceImpl(RequestPageCache pageCache,
                               BeanBlockOverrideSource overrideSource, Collection<BeanBlockContribution> configuration)
    {
        this.overrideSource = overrideSource;
        masterSource = new BeanBlockOverrideSourceImpl(pageCache, configuration);
    }

    public boolean hasDisplayBlock(String datatype)
    {
        return overrideSource.hasDisplayBlock(datatype) || masterSource.hasDisplayBlock(datatype);
    }

    public Block getDisplayBlock(String datatype)
    {
        Block result = overrideSource.getDisplayBlock(datatype);

        if (result == null)
            result = masterSource.getDisplayBlock(datatype);

        if (result == null)
            throw new RuntimeException(ServicesMessages.noDisplayForDataType(datatype));

        return result;
    }

    public Block getEditBlock(String datatype)
    {
        Block result = overrideSource.getEditBlock(datatype);

        if (result == null)
            result = masterSource.getEditBlock(datatype);

        if (result == null)
            throw new RuntimeException(ServicesMessages.noEditForDataType(datatype));

        return result;
    }

}
