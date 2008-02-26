package org.apache.tapestry.grid;

/**
 * Identifies how a column within a {@link org.apache.tapestry.grid.GridSortModel} is sorted.
 */
public enum ColumnSort
{
    /**
     * A sort column and sorted in ascending order.
     */
    ASCENDING,

    /**
     * A sort column, and sorted in descending order.
     */
    DESCENDING,

    /**
     * Not a sort column.
     */
    UNSORTED
}
