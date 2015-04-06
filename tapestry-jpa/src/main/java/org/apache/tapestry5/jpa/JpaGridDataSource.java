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

package org.apache.tapestry5.jpa;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * A simple implementation of {@link org.apache.tapestry5.grid.GridDataSource} based on a
 * {@linkplain javax.persistence.EntityManager} and a known
 * entity class. This implementation does support multiple
 * {@link org.apache.tapestry5.grid.SortConstraint sort
 * constraints}.
 *
 * This class is <em>not</em> thread-safe; it maintains internal state.
 *
 * Typically, an instance of this object is created fresh as needed (that is, it is not stored
 * between requests).
 *
 * @since 5.3
 */
public class JpaGridDataSource<E> implements GridDataSource
{

    private final EntityManager entityManager;

    private final Class<E> entityType;

    private int startIndex;

    private List<E> preparedResults;

    public JpaGridDataSource(final EntityManager entityManager, final Class<E> entityType)
    {
        super();
        this.entityManager = entityManager;
        this.entityType = entityType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAvailableRows()
    {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> criteria = builder.createQuery(Long.class);

        final Root<E> root = criteria.from(entityType);

        criteria = criteria.select(builder.count(root));

        applyAdditionalConstraints(criteria, root, builder);

        return entityManager.createQuery(criteria).getSingleResult().intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(final int startIndex, final int endIndex,
            final List<SortConstraint> sortConstraints)
    {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        final CriteriaQuery<E> criteria = builder.createQuery(entityType);

        final Root<E> root = criteria.from(entityType);

        applyAdditionalConstraints(criteria.select(root), root, builder);

        for (final SortConstraint constraint : sortConstraints)
        {

            final String propertyName = constraint.getPropertyModel().getPropertyName();

            final Path<Object> propertyPath = root.get(propertyName);

            switch (constraint.getColumnSort())
            {

                case ASCENDING:

                    criteria.orderBy(builder.asc(propertyPath));
                    break;

                case DESCENDING:
                    criteria.orderBy(builder.desc(propertyPath));
                    break;

                default:
            }
        }

        final TypedQuery<E> query = entityManager.createQuery(criteria);

        query.setFirstResult(startIndex);
        query.setMaxResults(endIndex - startIndex + 1);

        this.startIndex = startIndex;

        preparedResults = query.getResultList();

    }

    protected void applyAdditionalConstraints(final CriteriaQuery<?> criteria, final Root<E> root,
            final CriteriaBuilder builder)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getRowValue(final int index)
    {
        return preparedResults.get(index - startIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<E> getRowType()
    {
        return entityType;
    }

}
