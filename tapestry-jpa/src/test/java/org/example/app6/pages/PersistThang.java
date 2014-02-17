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

import static org.example.app6.AppConstants.TEST_PERSISTENCE_UNIT_2;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.jpa.JpaPersistenceConstants;
import org.apache.tapestry5.jpa.annotations.CommitAfter;
import org.example.app6.entities.Thang;

public class PersistThang
{
    @PersistenceContext(unitName = TEST_PERSISTENCE_UNIT_2)
    private EntityManager entityManager;

    @Persist(JpaPersistenceConstants.ENTITY)
    @Property
    private Thang thang;

    @CommitAfter
    @PersistenceContext(unitName = TEST_PERSISTENCE_UNIT_2)
    void onCreateEntity()
    {
        final Thang thang = new Thang();
        thang.setName("name");

        entityManager.persist(thang);

        this.thang = thang;
    }

    void onChangeName()
    {
        thang.setName("name2");

        // No commit, so no real change.
    }

    void onSetToTransient()
    {
        thang = new Thang();
    }

    void onSetToNull()
    {
        thang = null;
    }

    @CommitAfter
    @PersistenceContext(unitName = TEST_PERSISTENCE_UNIT_2)
    void onDelete()
    {
        final List<Thang> thangs = entityManager.createQuery("select t from Thang t").getResultList();

        entityManager.remove(thangs.get(0));
    }
}
