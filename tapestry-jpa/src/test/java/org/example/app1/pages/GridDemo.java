// Copyright 2008 The Apache Software Foundation
//
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

package org.example.app1.pages;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.jpa.JpaGridDataSource;
import org.apache.tapestry5.jpa.annotations.CommitAfter;
import org.example.app1.AppConstants;
import org.example.app1.entities.User;
import org.example.app1.services.UserDAO;

public class GridDemo
{
    @PersistenceContext(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    private EntityManager entityManager;

    @Inject
    private UserDAO userDAO;

    public GridDataSource getSource()
    {
        return new JpaGridDataSource<User>(entityManager, User.class)
        {
            @Override
            protected void applyAdditionalConstraints(final CriteriaQuery<?> criteria,
                    final Root<User> root, final CriteriaBuilder builder)
            {
                criteria.where(builder.equal(root.get("lastName"), "User"));
            }
        };
    }

    @CommitAfter
    @PersistenceContext(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    void onActionFromSetup()
    {
        userDAO.deleteAll();

        for (int i = 1; i <= 20; i++)
        {
            final User user = new User();

            final String suffix = String.valueOf(i);

            user.setFirstName("Joe_" + suffix);
            user.setLastName("User");
            user.setEncodedPassword("####");
            user.setEmail("joe" + suffix + "@null.com");

            entityManager.persist(user);
        }

    }
}
