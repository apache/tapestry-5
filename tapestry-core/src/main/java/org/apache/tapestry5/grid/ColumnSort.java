package org.apache.tapestry5.grid;

/**
 * Identifies how a column within a {@link org.apache.tapestry5.grid.GridSortModel} is sorted.
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
