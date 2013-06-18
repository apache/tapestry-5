// Copyright 2007, 2010, 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.services;

import org.apache.tapestry5.corelib.components.Label;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

/**
 * A contribution to the {@link BeanBlockSource} service, defining a page name and block id (within the page) that can
 * edit or display a particular type of property.
 */
public class BeanBlockContribution
{
    private final String dataType;

    private final String pageName;

    private final String blockId;

    private final boolean edit;

    protected BeanBlockContribution(String dataType, String pageName, String blockId, boolean edit)
    {
        assert InternalUtils.isNonBlank(dataType);
        assert InternalUtils.isNonBlank(pageName);
        assert InternalUtils.isNonBlank(blockId);
        this.dataType = dataType;
        this.pageName = pageName;
        this.blockId = blockId;
        this.edit = edit;
    }

    /**
     * The type of data for which the indicated block will provide an editor or displayer for.
     */
    public final String getDataType()
    {
        return dataType;
    }

    /**
     * The id of the block within the page.
     */
    public final String getBlockId()
    {
        return blockId;
    }

    /**
     * If true, then the block provides an editor for the property, consisting of a {@link Label} and some field
     * component (or set of field components). If false, the block is used to display the value of the property, usually
     * by applying some kind of formatting to the raw value.
     */
    public final boolean isEdit()
    {
        return edit;
    }

    /**
     * The logical name of the page containing the block.
     */
    public final String getPageName()
    {
        return pageName;
    }

}
