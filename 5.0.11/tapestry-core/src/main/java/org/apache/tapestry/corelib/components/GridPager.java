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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Link;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.grid.GridDataSource;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.annotations.Inject;

/**
 * Generates a series of links used to jump to a particular page index within the overall data set.
 */
public class GridPager
{
    /**
     * The source of the data displayed by the grid (this is used to determine {@link GridDataSource#getAvailableRows()
     * how many rows are available}, which in turn determines the page count).
     */
    @Parameter(required = true)
    private GridDataSource _source;

    /**
     * The number of rows displayed per page.
     */
    @Parameter(required = true)
    private int _rowsPerPage;

    /**
     * The current page number (indexed from 1).
     */
    @Parameter(required = true)
    private int _currentPage;

    /**
     * Number of pages before and after the current page in the range. The pager always displays links for 2 * range + 1
     * pages, unless that's more than the total number of available pages.
     */
    @Parameter("5")
    private int _range;

    private int _lastIndex;

    private int _maxPages;

    @Inject
    private ComponentResources _resources;

    @Inject
    private Messages _messages;

    void beginRender(MarkupWriter writer)
    {
        int availableRows = _source.getAvailableRows();

        _maxPages = ((availableRows - 1) / _rowsPerPage) + 1;

        if (_maxPages < 2) return;

        writer.element("div", "class", "t-data-grid-pager");

        _lastIndex = 0;

        for (int i = 1; i <= 2; i++)
            writePageLink(writer, i);

        int low = _currentPage - _range;
        int high = _currentPage + _range;

        if (low < 1)
        {
            low = 1;
            high = 2 * _range + 1;
        }
        else
        {
            if (high > _maxPages)
            {
                high = _maxPages;
                low = high - 2 * _range;
            }
        }

        for (int i = low; i <= high; i++)
            writePageLink(writer, i);

        for (int i = _maxPages - 1; i <= _maxPages; i++)
            writePageLink(writer, i);

        writer.end();
    }

    private void writePageLink(MarkupWriter writer, int pageIndex)
    {
        if (pageIndex < 1 || pageIndex > _maxPages) return;

        if (pageIndex <= _lastIndex) return;

        if (pageIndex != _lastIndex + 1) writer.write(" ... "); // &#8230; is ellipsis

        _lastIndex = pageIndex;

        if (pageIndex == _currentPage)
        {
            writer.element("span", "class", "current");
            writer.write(Integer.toString(pageIndex));
            writer.end();
            return;
        }

        Link link = _resources.createActionLink(TapestryConstants.ACTION_EVENT, false, pageIndex);

        writer.element("a", "href", link, "title", _messages.format("goto-page", pageIndex));

        writer.write(Integer.toString(pageIndex));
        writer.end();

    }

    void onAction(int newPage)
    {
        // TODO: Validate newPage in range

        _currentPage = newPage;
    }
}
