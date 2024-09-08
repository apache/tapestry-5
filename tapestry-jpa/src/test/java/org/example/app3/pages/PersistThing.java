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

package org.example.app3.pages;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.jpa.JpaPersistenceConstants;
import org.apache.tapestry5.jpa.annotations.CommitAfter;
import org.example.app3.model.Thing;

public class PersistThing
{
    @PersistenceContext
    private EntityManager entityManager;

    @Persist(JpaPersistenceConstants.ENTITY)
    @Property
    private Thing thing;

    @CommitAfter
    void onCreateEntity()
    {
        final Thing thing = new Thing();
        thing.setName("name");

        entityManager.persist(thing);

        this.thing = thing;
    }
}
