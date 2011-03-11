// Copyright 2011 The Apache Software Foundation
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

package org.example.app.pages;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnit;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.jpa.annotations.CommitAfter;
import org.example.app.AppConstants;
import org.example.app.entities.User;
import org.example.app.services.UserDAO;

public class EncodeEntities
{
    @PersistenceUnit(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    private EntityManager entityManager;

    @Inject
    private UserDAO userDAO;

    @SuppressWarnings("unused")
    @Property
    private User user;

    @CommitAfter
    @PersistenceUnit(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    void onCreate()
    {
        final User user = new User();
        user.setFirstName("name");

        entityManager.persist(user);
    }

    @SuppressWarnings("unchecked")
    User onPassivate()
    {
        // Use ordering so that we get the most recently inserted users first.
        final List<User> users = userDAO.findAll();
        if (users.isEmpty())
            return null;

        return users.get(0);
    }

    void onActivate(final User user)
    {
        this.user = user;
    }
}
