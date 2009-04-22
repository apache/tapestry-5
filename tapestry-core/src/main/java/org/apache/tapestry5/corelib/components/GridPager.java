// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ClientBehaviorSupport;

/**
 * Generates a series of links used to jump to a particular page index within the overall data set.
 */
@Events(InternalConstants.GRID_INPLACE_UPDATE + " (internal event)")
public class GridPager
{
    /**
     * The source of the data displayed by the grid (this is used to determine {@link GridDataSource#getAvailableRows()
     * how many rows are available}, which in turn determines the page count).
     */
    @Parameter(required = true)
    private GridDataSource source;

    /**
     * The number of rows displayed per page.
     */
    @Parameter(required = true)
    private int rowsPerPage;

    /**
     * The current page number (indexed from 1).
     */
    @Parameter(required = true)
    private int currentPage;

    /**
     * Number of pages before and after the current page in the range. The pager always displays links for 2 * range + 1
     * pages, unless that's more than the total number of available pages.
     */
    @Parameter("5")
    private int range;

    /**
     * If not null, then each link is output as a link to update the specified zone.
     */
    @Parameter
    private String zone;

    private int lastIndex;

    private int maxPages;

    @Inject
    private ComponentResources resources;

    @Inject
    private Messages messages;

    @Environmental
    private ClientBehaviorSupport clientBehaviorSupport;

    @Environmental
    private RenderSupport renderSupport;

    void beginRender(MarkupWriter writer)
    {
        int availableRows = source.getAvailableRows();

        maxPages = ((availableRows - 1) / rowsPerPage) + 1;

        if (maxPages < 2) return;

        writer.element("div", "class", "t-data-grid-pager");

        lastIndex = 0;

        for (int i = 1; i <= 2; i++)
            writePageLink(writer, i);

        int low = currentPage - range;
        int high = currentPage + range;

        if (low < 1)
        {
            low = 1;
            high = 2 * range + 1;
        }
        else
        {
            if (high > maxPages)
            {
                high = maxPages;
                low = high - 2 * range;
            }
        }

        for (int i = low; i <= high; i++)
            writePageLink(writer, i);

        for (int i = maxPages - 1; i <= maxPages; i++)
            writePageLink(writer, i);

        writer.end();
    }

    private void writePageLink(MarkupWriter writer, int pageIndex)
    {
        if (pageIndex < 1 || pageIndex > maxPages) return;

        if (pageIndex <= lastIndex) return;

        if (pageIndex != lastIndex + 1) writer.write(" ... ");

        lastIndex = pageIndex;

        if (pageIndex == currentPage)
        {
            writer.element("span", "class", "current");
            writer.write(Integer.toString(pageIndex));
            writer.end();
            return;
        }

        Object[] context = zone == null
                           ? new Object[] { pageIndex }
                           : new Object[] { pageIndex, zone };

        Link link = resources.createEventLink(EventConstants.ACTION, context);

        Element element = writer.element("a",
                                         "href", zone == null ? link : "#",
                                         "title", messages.format("goto-page", pageIndex));

        writer.write(Integer.toString(pageIndex));
        writer.end();

        if (zone != null)
        {
            String id = renderSupport.allocateClientId(resources);

            element.attribute("id", id);

            clientBehaviorSupport.linkZone(id, zone, link);
        }
    }

    /**
     * Normal, non-Ajax event handler.
     */
    void onAction(int newPage)
    {
        // TODO: Validate newPage in range

        currentPage = newPage;
    }

    /**
     * Akjax event handler, passing the zone along.
     */
    boolean onAction(int newPage, String zone)
    {
        onAction(newPage);

        resources.triggerEvent(InternalConstants.GRID_INPLACE_UPDATE, new Object[] { zone }, null);

        return true; // abort event
    }
}
