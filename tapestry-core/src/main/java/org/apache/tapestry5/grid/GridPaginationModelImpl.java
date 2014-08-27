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

package org.apache.tapestry5.grid;

import org.apache.tapestry5.BaseOptimizedSessionPersistedObject;

/**
 * Standard implementation of {@link org.apache.tapestry5.grid.GridPaginationModel}.
 *
 * @since 5.4
 */
public class GridPaginationModelImpl extends BaseOptimizedSessionPersistedObject implements GridPaginationModel
{
    private static final long serialVersionUID = -5532310466213300537L;

    private String sortColumnId;

    private Boolean sortAscending;

    private Integer currentPage;

    @Override
    public String getSortColumnId()
    {
        return sortColumnId;
    }

    @Override
    public void setSortColumnId(String sortColumnId)
    {
        this.sortColumnId = sortColumnId;

        markDirty();
    }

    @Override
    public Boolean getSortAscending()
    {
        return sortAscending;
    }

    @Override
    public void setSortAscending(Boolean sortAscending)
    {
        this.sortAscending = sortAscending;

        markDirty();
    }

    @Override
    public Integer getCurrentPage()
    {
        return currentPage;
    }

    @Override
    public void setCurrentPage(Integer currentPage)
    {
        this.currentPage = currentPage;

        markDirty();
    }

    @Override
    public String toString()
    {
        return String.format("GridPaginationModelImpl[currentPage=%s, sortColumnId=%s, sortAscending=%s]",
                currentPage, sortColumnId, sortAscending);
    }
}
