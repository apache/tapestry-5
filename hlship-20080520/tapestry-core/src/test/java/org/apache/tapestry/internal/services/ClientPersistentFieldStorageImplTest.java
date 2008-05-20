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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Link;
import org.apache.tapestry.internal.util.Holder;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry.services.PersistentFieldChange;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.test.TapestryTestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;
import org.easymock.IAnswer;
import org.testng.annotations.Test;

import java.util.List;

public class ClientPersistentFieldStorageImplTest extends TapestryTestCase
{
    @Test
    public void no_client_data_in_request()
    {
        Request request = mockRequest(null);
        Link link = mockLink();

        replay();

        ClientPersistentFieldStorage storage = new ClientPersistentFieldStorageImpl(request);

        // Should do nothing.

        storage.updateLink(link);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void store_and_restore_a_change()
    {
        Request request = mockRequest(null);
        Link link = mockLink();
        final Holder<String> holder = Holder.create();

        String pageName = "Foo";
        String componentId = "bar.baz";
        String fieldName = "biff";
        Object value = 99;

        // Use an IAnswer to capture the value.

        link.addParameter(eq(ClientPersistentFieldStorageImpl.PARAMETER_NAME), isA(String.class));
        getMocksControl().andAnswer(new IAnswer<Void>()
        {
            public Void answer() throws Throwable
            {
                String base64 = (String) EasyMock.getCurrentArguments()[1];

                holder.put(base64);

                return null;
            }
        });

        replay();

        ClientPersistentFieldStorage storage1 = new ClientPersistentFieldStorageImpl(request);

        storage1.postChange(pageName, componentId, fieldName, value);

        List<PersistentFieldChange> changes1 = newList(storage1.gatherFieldChanges(pageName));

        storage1.updateLink(link);

        verify();

        System.out.println(holder.get());

        assertEquals(changes1.size(), 1);
        PersistentFieldChange change1 = changes1.get(0);

        assertEquals(change1.getComponentId(), componentId);
        assertEquals(change1.getFieldName(), fieldName);
        assertEquals(change1.getValue(), value);

        // Now more training ...

        train_getParameter(request, ClientPersistentFieldStorageImpl.PARAMETER_NAME, holder.get());

        replay();

        ClientPersistentFieldStorage storage2 = new ClientPersistentFieldStorageImpl(request);

        List<PersistentFieldChange> changes2 = newList(storage2.gatherFieldChanges(pageName));

        verify();

        assertEquals(changes2.size(), 1);
        PersistentFieldChange change2 = changes2.get(0);

        assertEquals(change2.getComponentId(), componentId);
        assertEquals(change2.getFieldName(), fieldName);
        assertEquals(change2.getValue(), value);

        assertNotSame(change1, change2);
    }

    @Test
    public void multiple_changes()
    {
        Request request = mockRequest(null);
        Link link = mockLink();

        String pageName = "Foo";
        String componentId = "bar.baz";

        link.addParameter(eq(ClientPersistentFieldStorageImpl.PARAMETER_NAME), isA(String.class));

        replay();

        ClientPersistentFieldStorage storage = new ClientPersistentFieldStorageImpl(request);

        for (int k = 0; k < 3; k++)
        {
            for (int i = 0; i < 20; i++)
            {
                // Force some cache collisions ...

                storage.postChange(pageName, componentId, "field" + i, i * k);
            }
        }

        storage.postChange(pageName, null, "field", "foo");
        storage.postChange(pageName, null, "field", "bar");

        storage.updateLink(link);

        verify();
    }

    @Test
    public void null_value_is_a_remove()
    {
        Request request = mockRequest(null);
        Link link = mockLink();

        String pageName = "Foo";
        String componentId = "bar.baz";
        String fieldName = "woops";

        replay();

        ClientPersistentFieldStorage storage = new ClientPersistentFieldStorageImpl(request);

        storage.postChange(pageName, componentId, fieldName, 99);
        storage.postChange(pageName, componentId, fieldName, null);

        storage.updateLink(link);

        assertTrue(storage.gatherFieldChanges(pageName).isEmpty());

        verify();
    }

    /**
     * TAPESTRY-1475
     */
    @Test
    public void discard_changes()
    {
        Request request = mockRequest(null);
        Link link = mockLink();

        String pageName = "Foo";
        String componentId = "bar.baz";
        String fieldName = "woops";

        replay();

        ClientPersistentFieldStorage storage = new ClientPersistentFieldStorageImpl(request);

        storage.postChange(pageName, componentId, fieldName, 99);

        storage.discardChanges(pageName);

        storage.updateLink(link);

        assertTrue(storage.gatherFieldChanges(pageName).isEmpty());

        verify();
    }


    @Test
    public void value_not_serializable()
    {
        Request request = mockRequest(null);

        Object badBody = new Object()
        {
            @Override
            public String toString()
            {
                return "<BadBoy>";
            }
        };

        replay();

        ClientPersistentFieldStorage storage = new ClientPersistentFieldStorageImpl(request);

        try
        {
            storage.postChange("Foo", "bar.baz", "woops", badBody);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "State persisted on the client must be serializable, but <BadBoy> does not implement the Serializable interface.");
        }

        verify();
    }

    @Test
    public void corrupt_client_data()
    {
        // A cut-n-paste from some previous output, with the full value significantly truncated.
        Request request = mockRequest("H4sIAAAAAAAAAEWQsUoDQRCGJxdDTEwRU2hlZ71pBQ");

        replay();

        ClientPersistentFieldStorage storage = new ClientPersistentFieldStorageImpl(request);

        try
        {
            storage.postChange("Foo", "bar.baz", "woops", 99);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), ServicesMessages.corruptClientState());
            assertNotNull(ex.getCause());
        }

        verify();
    }

    protected final Request mockRequest(String clientData)
    {
        Request request = mockRequest();

        train_getParameter(request, ClientPersistentFieldStorageImpl.PARAMETER_NAME, clientData);

        return request;
    }

}
