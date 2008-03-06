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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.ComponentAction;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.Property;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.grid.GridConstants;
import org.apache.tapestry.grid.GridDataSource;
import org.apache.tapestry.grid.GridModel;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.services.FormSupport;

import java.util.List;

/**
 * Renders out a series of rows within the table.
 * <p/>
 * Inside a {@link Form}, a series of row index numbers are stored into the form ( {@linkplain FormSupport#store(Object,
 * ComponentAction) as ComponentActions}). This is not ideal ... in a situation where the data set can shift between the
 * form render and the form submission, this can cause unexpected results, including applying changes to the wrong
 * objects.
 */
public class GridRows
{
    private int _startRow;

    static class SetupForRow implements ComponentAction<GridRows>
    {
        private static final long serialVersionUID = -3216282071752371975L;

        private final int _rowIndex;

        public SetupForRow(int rowIndex)
        {
            _rowIndex = rowIndex;
        }

        public void execute(GridRows component)
        {
            component.setupForRow(_rowIndex);
        }
    }

    /**
     * Parameter used to set the CSS class for each row (each &lt;tr&gt; element) within the &lt;tbody&gt;). This is not
     * cached, so it will be recomputed for each row.
     */
    @Parameter(cache = false)
    private String _rowClass;

    /**
     * Object that provides access to the bean and data models used to render the Grid.
     */
    @Parameter(value = "componentResources.container")
    private GridModel _gridModel;

    /**
     * Number of rows displayed on each page. Long result sets are split across multiple pages.
     */
    @Parameter(required = true)
    private int _rowsPerPage;

    /**
     * The current page number within the available pages (indexed from 1).
     */
    @Parameter(required = true)
    private int _currentPage;

    /**
     * The current row being rendered, this is primarily an output parameter used to allow the Grid, and the Grid's
     * container, to know what object is being rendered.
     */
    @Parameter(required = true)
    @Property(write=false)
    private Object _row;

    /**
     * If true, then the CSS class on each &lt;TD&gt; cell will be omitted, which can reduce the amount of output from
     * the component overall by a considerable amount. Leave this as false, the default, when you are leveraging the CSS
     * to customize the look and feel of particular columns.
     */
    @Parameter
    private boolean _lean;

    /**
     * If true and the Loop is enclosed by a Form, then the normal state saving logic is turned off. Defaults to false,
     * enabling state saving logic within Forms.
     */
    @SuppressWarnings("unused")
    @Parameter
    private boolean _volatile;

    @Environmental(false)
    private FormSupport _formSupport;

    private boolean _recordingStateInsideForm;

    private int _endRow;

    private int _rowIndex;

    private String _propertyName;

    @Property(write=false)
    private PropertyModel _columnModel;

    public String getRowClass()
    {
        List<String> classes = CollectionFactory.newList();

        // Not a cached parameter, so careful to only access it once.

        String rc = _rowClass;

        if (rc != null) classes.add(rc);

        if (_rowIndex == _startRow) classes.add(GridConstants.FIRST_CLASS);

        if (_rowIndex == _endRow) classes.add(GridConstants.LAST_CLASS);

        return TapestryInternalUtils.toClassAttributeValue(classes);
    }

    public String getCellClass()
    {
        List<String> classes = CollectionFactory.newList();

        String id = _gridModel.getDataModel().get(_propertyName).getId();

        if (!_lean)
        {
            classes.add(id);

            switch (_gridModel.getSortModel().getColumnSort(id))
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
        GridDataSource dataSource = _gridModel.getDataSource();

        int availableRows = dataSource.getAvailableRows();

        int maxPages = ((availableRows - 1) / _rowsPerPage) + 1;

        // This can sometimes happen when the number of items shifts between requests.

        if (_currentPage > maxPages) _currentPage = maxPages;

        _startRow = (_currentPage - 1) * _rowsPerPage;
        _endRow = Math.min(availableRows - 1, _startRow + _rowsPerPage - 1);

        _rowIndex = _startRow;

        _recordingStateInsideForm = !_volatile && _formSupport != null;
    }

    /**
     * Callback method, used when recording state to a form, or called directly when not recording state.
     */
    void setupForRow(int rowIndex)
    {
        _row = _gridModel.getDataSource().getRowValue(rowIndex);

    }

    void beginRender()
    {
        // When needed, store a callback used when the form is submitted.

        if (_recordingStateInsideForm) _formSupport.store(this, new SetupForRow(_rowIndex));

        // And do it now for the render.

        setupForRow(_rowIndex);
    }

    boolean afterRender()
    {
        _rowIndex++;

        return _rowIndex > _endRow;
    }

    public List<String> getPropertyNames()
    {
        return _gridModel.getDataModel().getPropertyNames();
    }

    public String getPropertyName()
    {
        return _propertyName;
    }

    public void setPropertyName(String propertyName)
    {
        _propertyName = propertyName;

        _columnModel = _gridModel.getDataModel().get(propertyName);
    }
}
