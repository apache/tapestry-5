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

package org.apache.tapestry.services;

import org.apache.tapestry.corelib.components.Label;

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;

/**
 * A contribution to the {@link BeanBlockSource} service, defining a page name and block id (within
 * the page) that can edit or display a particular type of property.
 */
public final class BeanBlockContribution
{
    private final String _dataType;

    private final String _pageName;

    private final String _blockId;

    private final boolean _edit;

    public BeanBlockContribution(String dataType, String pageName, String blockId, boolean edit)
    {
        notBlank(dataType, "datatype");
        notBlank(pageName, "pageName");
        notBlank(blockId, "blockId");

        _dataType = dataType;
        _pageName = pageName;
        _blockId = blockId;
        _edit = edit;
    }

    /**
     * The type of data for which the indicated block will provide an editor or displayer for.
     */
    public String getDataType()
    {
        return _dataType;
    }

    /** The id of the block within the page. */
    public String getBlockId()
    {
        return _blockId;
    }

    /**
     * If true, then the block provides an editor for the property, consisting of a {@link Label}
     * and some field component (or set of field components). If false, the block is used to display
     * the value of the property, usually by applying some kind of formatting to the raw value.
     */
    public boolean isEdit()
    {
        return _edit;
    }

    /** The logical name of the page containing the block. */
    public String getPageName()
    {
        return _pageName;
    }

}
