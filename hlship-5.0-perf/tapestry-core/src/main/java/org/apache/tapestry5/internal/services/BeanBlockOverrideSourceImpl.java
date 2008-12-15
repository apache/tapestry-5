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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.BeanBlockContribution;
import org.apache.tapestry5.services.BeanBlockOverrideSource;

import java.util.Collection;
import java.util.Map;

public class BeanBlockOverrideSourceImpl implements BeanBlockOverrideSource
{
    private final RequestPageCache pageCache;

    private final Map<String, BeanBlockContribution> display = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, BeanBlockContribution> edit = CollectionFactory.newCaseInsensitiveMap();

    public BeanBlockOverrideSourceImpl(RequestPageCache pageCache,
                                       Collection<BeanBlockContribution> configuration)
    {
        this.pageCache = pageCache;

        for (BeanBlockContribution contribution : configuration)
        {
            Map<String, BeanBlockContribution> map = contribution.isEdit() ? edit : display;

            map.put(contribution.getDataType(), contribution);
        }
    }

    public boolean hasDisplayBlock(String datatype)
    {
        return display.containsKey(datatype);
    }

    public Block getDisplayBlock(String datatype)
    {
        return toBlock(display.get(datatype));
    }

    private Block toBlock(BeanBlockContribution contribution)
    {
        if (contribution == null) return null;

        Page page = pageCache.get(contribution.getPageName());

        return page.getRootElement().getBlock(contribution.getBlockId());
    }

    public Block getEditBlock(String datatype)
    {
        return toBlock(edit.get(datatype));
    }

}
