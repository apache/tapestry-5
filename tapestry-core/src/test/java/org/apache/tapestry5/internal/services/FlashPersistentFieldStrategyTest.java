// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.PersistentFieldChange;
import org.apache.tapestry5.services.PersistentFieldStrategy;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Iterator;

/**
 * A more minimal test, since common behavior is already tested by {@link SessionPersistentFieldStrategyTest}.
 */
public class FlashPersistentFieldStrategyTest extends InternalBaseTestCase
{
    @Test
    public void post_change_to_root_component()
    {
        Session session = mockSession();
        Request request = mockRequest();
        Object value = new Object();

        train_getSession(request, true, session);

        session.setAttribute("flash:foo.Bar::field", value);

        replay();

        PersistentFieldStrategy strategy = new FlashPersistentFieldStrategy(request);

        strategy.postChange("foo.Bar", null, "field", value);

        verify();
    }

    @Test
    public void gather_changes_with_active_session()
    {
        Session session = mockSession();
        Request request = mockRequest();

        train_getSession(request, false, session);
        train_getAttributeNames(
                session,
                "flash:foo.Bar:",
                "flash:foo.Bar::root",
                "flash:foo.Bar:nested:down");

        train_getAttribute(session, "flash:foo.Bar::root", "ROOT");
        session.setAttribute("flash:foo.Bar::root", null);

        train_getAttribute(session, "flash:foo.Bar:nested:down", "DOWN");
        session.setAttribute("flash:foo.Bar:nested:down", null);

        replay();

        PersistentFieldStrategy strategy = new FlashPersistentFieldStrategy(request);

        Collection<PersistentFieldChange> changes = strategy.gatherFieldChanges("foo.Bar");

        assertEquals(changes.size(), 2);

        Iterator<PersistentFieldChange> i = changes.iterator();

        PersistentFieldChange change1 = i.next();

        assertEquals(change1.getComponentId(), "");
        assertEquals(change1.getFieldName(), "root");
        assertEquals(change1.getValue(), "ROOT");

        PersistentFieldChange change2 = i.next();

        assertEquals(change2.getComponentId(), "nested");
        assertEquals(change2.getFieldName(), "down");
        assertEquals(change2.getValue(), "DOWN");

        verify();
    }
}
