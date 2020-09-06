// Copyright 2008, 2014 The Apache Software Foundation
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

package org.apache.tapestry5.hibernate.web.internal;

import org.apache.tapestry5.hibernate.web.internal.EntityPersistentFieldStrategy;
import org.apache.tapestry5.hibernate.web.internal.SessionRestorable;
import org.apache.tapestry5.test.TapestryTestCase;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.testng.annotations.Test;

@Test
public class EntityPersistentFieldStrategyTest extends TapestryTestCase
{
    public void not_an_entity()
    {
        String nonEntity = "foo";
        Session session = newMock(Session.class);
        EntityPersistentFieldStrategy strategy = new EntityPersistentFieldStrategy(session, null);

        expect(session.contains(nonEntity)).andReturn(true);
        expect(session.getEntityName(nonEntity)).andThrow(new HibernateException("error"));

        replay();

        try
        {
            strategy.postChange("pageName", "", "fieldName", nonEntity);

            unreachable();
        } catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Failed persisting an entity in the session. entity: foo");
        }

        verify();
    }

    public void transient_entity()
    {
        SampleEntity entity = new SampleEntity();
        Session session = newMock(Session.class);
        EntityPersistentFieldStrategy strategy = new EntityPersistentFieldStrategy(session, null);

        expect(session.contains(entity)).andReturn(false);

        replay();

        Object persisted = strategy.convertApplicationValueToPersisted(entity);

        assert persisted instanceof SessionRestorable;

        Object restored = strategy.convertPersistedToApplicationValue(persisted);

        assertSame(entity, restored);

        verify();
    }

    public void persistent_entity()
    {
        SampleEntity entity = new SampleEntity();
        SampleEntity restoredEntity = new SampleEntity();
        Session session = newMock(Session.class);
        EntityPersistentFieldStrategy strategy = new EntityPersistentFieldStrategy(session, null);

        expect(session.contains(entity)).andReturn(true);
        expect(session.getEntityName(entity)).andReturn("SampleEntity");
        expect(session.getIdentifier(entity)).andReturn(42);

        expect(session.get("SampleEntity", 42)).andReturn(restoredEntity);

        replay();

        Object persisted = strategy.convertApplicationValueToPersisted(entity);

        assert persisted instanceof SessionRestorable;

        Object restored = strategy.convertPersistedToApplicationValue(persisted);

        assertSame(restored, restoredEntity);

        verify();

    }
}
