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

package org.example.app1.services.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.example.app1.AppConstants;
import org.example.app1.entities.User;
import org.example.app1.services.UserDAO;

public class UserDAOImpl implements UserDAO
{
    @Inject
    @PersistenceContext(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    private EntityManager entityManager;

    @Override
    public void add(final User user)
    {
        entityManager.persist(user);
    }

    @Override
    @SuppressWarnings(
    { "unchecked" })
    public List<User> findAll()
    {
        return entityManager.createQuery("select u from User u order by u.id desc").getResultList();
    }

    @Override
    public void delete(final User... users)
    {
        for (final User user : users)
            entityManager.remove(user);
    }

    @Override
    public void deleteAll()
    {
        for (final User u : findAll())
        {
            entityManager.remove(u);
        }
    }
}
