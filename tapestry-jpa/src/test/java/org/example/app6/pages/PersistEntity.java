// Copyright 2014 The Apache Software Foundation
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

package org.example.app6.pages;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.jpa.JpaPersistenceConstants;
import org.apache.tapestry5.jpa.annotations.CommitAfter;
import org.example.app6.AppConstants;
import org.example.app6.entities.User;

public class PersistEntity
{
    @PersistenceContext(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    private EntityManager entityManager;

    @Persist(JpaPersistenceConstants.ENTITY)
    @Property
    private User user;

    @CommitAfter
    @PersistenceContext(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    void onCreateEntity()
    {
        final User user = new User();
        user.setFirstName("name");

        entityManager.persist(user);

        this.user = user;
    }

    void onChangeName()
    {
        user.setFirstName("name2");

        // No commit, so no real change.
    }

    void onSetToTransient()
    {
        user = new User();
    }

    void onSetToNull()
    {
        user = null;
    }

    @CommitAfter
    @PersistenceContext(unitName = AppConstants.TEST_PERSISTENCE_UNIT)
    void onDelete()
    {
        final List<User> users = entityManager.createQuery("select u from User u").getResultList();

        entityManager.remove(users.get(0));
    }
}
