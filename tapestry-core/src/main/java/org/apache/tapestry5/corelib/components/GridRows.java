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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.PrimaryKeyEncoder;
import org.apache.tapestry5.PropertyOverrides;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.grid.GridConstants;
import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.GridModel;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.FormSupport;

import java.io.Serializable;
import java.util.List;

/**
 * Renders out a series of rows within the table.
 * <p/>
 * Inside a {@link Form}, a series of row index numbers are stored into the form ( {@linkplain FormSupport#store(Object,
 * ComponentAction) as ComponentActions}). This is not ideal ... in a situation where the data set can shift between the
 * form render and the form submission, this can cause unexpected results, including applying changes to the wrong
 * objects.
 */
@SuppressWarnings({"unchecked"})
public class GridRows
{
    private int startRow;

    private boolean recordStateByIndex;
    private boolean recordStateByEncoder;

    /**
     * This action is used when a {@link org.apache.tapestry5.PrimaryKeyEncoder} is not.
     */
    static class SetupForRowByIndex implements ComponentAction<GridRows>
    {
        private static final long serialVersionUID = -3216282071752371975L;

        private final int rowIndex;

        public SetupForRowByIndex(int rowIndex)
        {
            this.rowIndex = rowIndex;
        }

        public void execute(GridRows component)
        {
            component.setupForRow(rowIndex);
        }

        @Override
        public String toString()
        {
            return String.format("GridRows.SetupForRowByIndex[%d]", rowIndex);
        }
    }

    /**
     * This action is used when a {@link org.apache.tapestry5.PrimaryKeyEncoder} is provided.
     */
    static class SetupForRowByKey implements ComponentAction<GridRows>
    {
        private final Serializable rowKey;

        SetupForRowByKey(Serializable rowKey)
        {
            this.rowKey = rowKey;
        }

        public void execute(GridRows component)
        {
            component.setupForRowByKey(rowKey);
        }

        @Override
        public String toString()
        {
            return String.format("GridRows.SetupForRowByKey[%s]", rowKey);
        }
    }


    /**
     * This action is also associated with the {@link org.apache.tapestry5.PrimaryKeyEncoder}; it allows the PKE to be
     * informed of the series of keys to expect with the form submission.
     */
    static class PrepareForKeys implements ComponentAction<GridRows>
    {
        private List<Serializable> storedKeys;

        public PrepareForKeys(List<Serializable> storedKeys)
        {
            this.storedKeys = storedKeys;
        }

        public void execute(GridRows component)
        {
            component.prepareForKeys(storedKeys);
        }

        @Override
        public String toString()
        {
            return "GridRows.PrepareForKeys" + storedKeys.toString();
        }
    }

    /**
     * Parameter used to set the CSS class for each row (each &lt;tr&gt; element) within the &lt;tbody&gt;). This is not
     * cached, so it will be recomputed for each row.
     */
    @Parameter(cache = false)
    private String rowClass;

    /**
     * Object that provides access to the bean and data models used to render the Grid.
     */
    @Parameter(value = "componentResources.container")
    private GridModel gridModel;

    /**
     * Where to search for property override blocks.
     */
    @Parameter(required = true, allowNull = false)
    @Property
    private PropertyOverrides overrides;

    /**
     * Number of rows displayed on each page. Long result sets are split across multiple pages.
     */
    @Parameter(required = true)
    private int rowsPerPage;

    /**
     * The current page number within the available pages (indexed from 1).
     */
    @Parameter(required = true)
    private int currentPage;

    /**
     * The current row being rendered, this is primarily an output parameter used to allow the Grid, and the Grid's
     * container, to know what object is being rendered.
     */
    @Parameter(required = true)
    @Property(write = false)
    private Object row;

    /**
     * If true, then the CSS class on each &lt;TD&gt; cell will be omitted, which can reduce the amount of output from
     * the component overall by a considerable amount. Leave this as false, the default, when you are leveraging the CSS
     * to customize the look and feel of particular columns.
     */
    @Parameter
    private boolean lean;

    /**
     * If true and the Loop is enclosed by a Form, then the normal state saving logic is turned off. Defaults to false,
     * enabling state saving logic within Forms.
     */
    @Parameter(name = "volatile")
    private boolean volatileState;

    /**
     * Changes how state is recorded into the form to store the {@linkplain org.apache.tapestry5.PrimaryKeyEncoder#toKey(Object)
     * primary key} for each row (rather than the index), and restore the {@linkplain
     * org.apache.tapestry5.PrimaryKeyEncoder#toValue(java.io.Serializable) row values} from the primary keys.
     */
    @Parameter
    private PrimaryKeyEncoder encoder;

    /**
     * Optional output parameter (only set during rendering) that identifies the current row index. This is the index on
     * the page (i.e., always numbered from zero) as opposed to the row index inside the {@link
     * org.apache.tapestry5.grid.GridDataSource}.
     */
    @Parameter
    private int rowIndex;

    /**
     * Optional output parameter that stores the current column index.
     */
    @Parameter
    @Property
    private int columnIndex;

    @Environmental(false)
    private FormSupport formSupport;


    private int endRow;

    /**
     * Index into the {@link org.apache.tapestry5.grid.GridDataSource}.
     */
    private int dataRowIndex;

    private String propertyName;

    @Property(write = false)
    private PropertyModel columnModel;

    private List<Serializable> encodedPrimaryKeys;

    public String getRowClass()
    {
        List<String> classes = CollectionFactory.newList();

        // Not a cached parameter, so careful to only access it once.

        String rc = rowClass;

        if (rc != null) classes.add(rc);

        if (dataRowIndex == startRow) classes.add(GridConstants.FIRST_CLASS);

        if (dataRowIndex == endRow) classes.add(GridConstants.LAST_CLASS);

        return TapestryInternalUtils.toClassAttributeValue(classes);
    }

    public String getCellClass()
    {
        List<String> classes = CollectionFactory.newList();

        String id = gridModel.getDataModel().get(propertyName).getId();

        if (!lean)
        {
            classes.add(id);

            switch (gridModel.getSortModel().getColumnSort(id))
            {
                case ASCENDING:
                    classes.add(GridConstants.SORT_ASCENDING_CLASS);
                    break;

                case DESCENDING:
                    classes.add(GridConstants.SORT_DESCENDING_CLASS);
                    break;

                default:
            }
        }


        return TapestryInternalUtils.toClassAttributeValue(classes);
    }

    void setupRender()
    {
        GridDataSource dataSource = gridModel.getDataSource();

        int availableRows = dataSource.getAvailableRows();

        int maxPages = ((availableRows - 1) / rowsPerPage) + 1;

        // This can sometimes happen when the number of items shifts between requests.

        if (currentPage > maxPages) currentPage = maxPages;

        startRow = (currentPage - 1) * rowsPerPage;
        endRow = Math.min(availableRows - 1, startRow + rowsPerPage - 1);

        dataRowIndex = startRow;

        boolean recordingStateInsideForm = !volatileState && formSupport != null;

        recordStateByIndex = recordingStateInsideForm && (encoder == null);
        recordStateByEncoder = recordingStateInsideForm && (encoder != null);

        if (recordStateByEncoder)
        {
            encodedPrimaryKeys = CollectionFactory.newList();

            // As we render, we'll fill in encodedPrimaryKeys.  That's ok, because nothing is serialized
            // until later.  When the form is submitted, this will give us a chance to inform
            // the PKE about the keys to expect.
            formSupport.store(this, new PrepareForKeys(encodedPrimaryKeys));
        }
    }

    /**
     * Callback method, used when recording state to a form, or called directly when not recording state.
     */
    void setupForRow(int rowIndex)
    {
        row = gridModel.getDataSource().getRowValue(rowIndex);
    }

    /**
     * Callback method that bypasses the data source and converts a primary key back into a row value (via {@link
     * org.apache.tapestry5.PrimaryKeyEncoder#toValue(java.io.Serializable)}).
     */
    void setupForRowByKey(Serializable rowKey)
    {
        row = encoder.toValue(rowKey);

        if (row == null)
            throw new IllegalArgumentException(
                    String.format("%s returned null for key %s.", encoder, rowKey));
    }

    /**
     * Callback method that allows the primary key encoder to prepare for the keys that will be resolved to row values
     * in this request.
     */
    private void prepareForKeys(List<Serializable> storedKeys)
    {
        encoder.prepareForKeys(storedKeys);
    }


    boolean beginRender()
    {
        // Setup for this row.

        setupForRow(dataRowIndex);

        // Update the index parameter (which starts from zero).
        rowIndex = dataRowIndex - startRow;


        if (row != null)
        {
            // When needed, store a callback used when the form is submitted.

            if (recordStateByIndex)
                formSupport.store(this, new SetupForRowByIndex(dataRowIndex));

            if (recordStateByEncoder)
            {
                Serializable key = encoder.toKey(row);
                encodedPrimaryKeys.add(key);

                formSupport.store(this, new SetupForRowByKey(key));
            }
        }

        // If the row is null, it's because the rowIndex is too large (see the notes
        // on GridDataSource).  When row is null, return false to not render anything for this iteration
        // of the loop.

        return row != null;
    }

    boolean afterRender()
    {
        dataRowIndex++;

        // Abort the loop when we hit a null row, or when we've exhausted the range we need to
        // display.

        return row == null || dataRowIndex > endRow;
    }

    public List<String> getPropertyNames()
    {
        return gridModel.getDataModel().getPropertyNames();
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;

        columnModel = gridModel.getDataModel().get(propertyName);
    }
}
