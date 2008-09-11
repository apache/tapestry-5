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

package org.apache.tapestry5.hibernate;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import java.util.List;

/**
 * A simple implementation of {@link org.apache.tapestry5.grid.GridDataSource} based on a Hibernate Session and a known
 * entity class.  This implementation does support multiple {@link org.apache.tapestry5.grid.SortConstraint sort
 * constraints}; however it assumes a direct mapping from sort constraint property to Hibernate property.
 * <p/>
 * This class is <em>not</em> thread-safe; it maintains internal state.
 * <p/>
 * Typically, an instance of this object is created fresh as needed (that is, it is not stored between requests).
 */
public class HibernateGridDataSource implements GridDataSource
{
    private final Session session;

    private final Class entityType;

    private int startIndex;

    private List preparedResults;

    public HibernateGridDataSource(Session session, Class entityType)
    {
        Defense.notNull(session, "session");
        Defense.notNull(entityType, "entityType");

        this.session = session;
        this.entityType = entityType;
    }

    /**
     * Returns the total number of rows for the configured entity type.
     */
    public int getAvailableRows()
    {
        Criteria criteria = session.createCriteria(entityType);

        applyAdditionalConstraints(criteria);

        criteria.setProjection(Projections.rowCount());

        Integer result = (Integer) criteria.uniqueResult();

        return result;
    }

    /**
     * Prepares the results, performing a query (applying the sort results, and the provided start and end index). The
     * results can later be obtained from {@link #getRowValue(int)} }.
     *
     * @param startIndex      index, from zero, of the first item to be retrieved
     * @param endIndex        index, from zero, of the last item to be retrieved
     * @param sortConstraints zero or more constraints used to set the order of the returned values
     */
    public void prepare(int startIndex, int endIndex, List<SortConstraint> sortConstraints)
    {
        Defense.notNull(sortConstraints, "sortConstraints");

        // We just assume that the property names in the SortContraint match the Hibernate
        // properties.

        Criteria crit = session.createCriteria(entityType);

        crit.setFirstResult(startIndex).setMaxResults(endIndex - startIndex + 1);

        for (SortConstraint constraint : sortConstraints)
        {

            String propertyName = constraint.getPropertyModel().getPropertyName();

            switch (constraint.getColumnSort())
            {

                case ASCENDING:

                    crit.addOrder(Order.asc(propertyName));
                    break;

                case DESCENDING:
                    crit.addOrder(Order.desc(propertyName));
                    break;

                default:
            }
        }

        applyAdditionalConstraints(crit);

        this.startIndex = startIndex;

        preparedResults = crit.list();
    }

    /**
     * Invoked after the main criteria has been set up (firstResult, maxResults and any sort contraints). This gives
     * subclasses a chance to apply additional constraints before the list of results is obtained from the criteria.
     * This implementation does nothing and may be overridden.
     */
    protected void applyAdditionalConstraints(Criteria crit)
    {
    }

    /**
     * Returns a row value at the given index (which must be within the range defined by the call to {@link
     * #prepare(int, int, java.util.List)} ).
     *
     * @param index of object
     * @return object at that index
     */
    public Object getRowValue(int index)
    {
        return preparedResults.get(index - startIndex);
    }

    /**
     * Returns the entity type, as provided via the constructor.
     */
    public Class getRowType()
    {
        return entityType;
    }
}
