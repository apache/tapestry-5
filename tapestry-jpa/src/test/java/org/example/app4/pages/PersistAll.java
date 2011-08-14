// Copyright 2011 The Apache Software Foundation
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

package org.example.app4.pages;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.jpa.JpaPersistenceConstants;
import org.apache.tapestry5.jpa.annotations.CommitAfter;
import org.example.app1.entities.Thang;
import org.example.app1.entities.User;
import org.example.app2.entities.Item;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class PersistAll
{
    @PersistenceContext
    private EntityManager entityManager;


    @Persist(JpaPersistenceConstants.ENTITY)
    @Property
    private User user;

    @Persist(JpaPersistenceConstants.ENTITY)
    @Property
    private Item item;

    @Persist(JpaPersistenceConstants.ENTITY)
    @Property
    private Thang thang;

    @CommitAfter
    void onCreateUser()
    {
        final User user = new User();
        user.setFirstName("Foo User");

        entityManager.persist(user);

        this.user = user;
    }

    @CommitAfter
    void onCreateItem()
    {
        final Item item = new Item();
        item.setName("Bar Item");

        entityManager.persist(item);

        this.item = item;
    }

    @CommitAfter
    void onCreateThang()
    {
        final Thang thang = new Thang();
        thang.setName("Baz Thang");

        entityManager.persist(thang);

        this.thang = thang;
    }
}
