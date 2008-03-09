// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.hibernate;

import org.apache.tapestry.test.TapestryTestCase;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.testng.annotations.Test;

@Test
public class EntityPersistentFieldStrategyTest extends TapestryTestCase {
	public void not_an_entity() {
		Session session = newMock(Session.class);
		EntityPersistentFieldStrategy strategy = new EntityPersistentFieldStrategy(session, null, null);
		
		expect(session.getEntityName("foo")).andThrow(new HibernateException("error"));
		replay();
		try {
			strategy.postChange(null, null, null, "foo");
			fail("did not throw");
		} catch (IllegalArgumentException e) { }
		verify();		
	}
}
