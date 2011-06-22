// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.Link;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.ClientDataSink;
import org.apache.tapestry5.services.PersistentFieldChange;
import org.apache.tapestry5.services.Request;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Manages client-persistent values on behalf of a {@link ClientPersistentFieldStorageImpl}. Some effort is made to
 * ensure that we don't uncessarily convert between objects and Base64 (the encoding used to record the value on the
 * client).
 */
@Scope(ScopeConstants.PERTHREAD)
public class ClientPersistentFieldStorageImpl implements ClientPersistentFieldStorage
{
    static final String PARAMETER_NAME = "t:state:client";

    private static class Key implements Serializable
    {
        private static final long serialVersionUID = -2741540370081645945L;

        private final String pageName;

        private final String componentId;

        private final String fieldName;

        Key(String pageName, String componentId, String fieldName)
        {
            this.pageName = pageName;
            this.componentId = componentId;
            this.fieldName = fieldName;
        }

        public boolean matches(String pageName)
        {
            return this.pageName.equals(pageName);
        }

        public PersistentFieldChange toChange(Object value)
        {
            return new PersistentFieldChangeImpl(componentId == null ? "" : componentId,
                                                 fieldName, value);
        }

        @Override
        public int hashCode()
        {
            final int PRIME = 31;

            int result = 1;

            result = PRIME * result + ((componentId == null) ? 0 : componentId.hashCode());

            // fieldName and pageName are never null

            result = PRIME * result + fieldName.hashCode();
            result = PRIME * result + pageName.hashCode();

            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final Key other = (Key) obj;

            // fieldName and pageName are never null

            if (!fieldName.equals(other.fieldName)) return false;
            if (!pageName.equals(other.pageName)) return false;

            if (componentId == null)
            {
                if (other.componentId != null) return false;
            }
            else if (!componentId.equals(other.componentId)) return false;

            return true;
        }
    }

    private final ClientDataEncoder clientDataEncoder;

    private final Map<Key, Object> persistedValues = CollectionFactory.newMap();

    private String clientData;

    private boolean mapUptoDate = false;

    public ClientPersistentFieldStorageImpl(Request request, ClientDataEncoder clientDataEncoder)
    {
        this.clientDataEncoder = clientDataEncoder;

        // This, here, is the problem of TAPESTRY-2501; this call can predate
        // the check to set the character set based on meta data of the page.

        String value = request.getParameter(PARAMETER_NAME);

        // MIME can encode to a '+' character; the browser converts that to a space; we convert it
        // back.

        clientData = value == null ? null : value.replace(' ', '+');
    }

    public void updateLink(Link link)
    {
        refreshClientData();

        if (clientData != null) link.addParameter(PARAMETER_NAME, clientData);
    }

    public Collection<PersistentFieldChange> gatherFieldChanges(String pageName)
    {
        refreshMap();

        if (persistedValues.isEmpty()) return Collections.emptyList();

        Collection<PersistentFieldChange> result = CollectionFactory.newList();

        for (Map.Entry<Key, Object> e : persistedValues.entrySet())
        {
            Key key = e.getKey();

            if (key.matches(pageName)) result.add(key.toChange(e.getValue()));
        }

        return result;
    }

    public void discardChanges(String pageName)
    {
        refreshMap();

        Collection<Key> removedKeys = CollectionFactory.newList();

        for (Key key : persistedValues.keySet())
        {
            if (key.pageName.equals(pageName)) removedKeys.add(key);
        }

        for (Key key : removedKeys)
        {
            persistedValues.remove(key);
            clientData = null;
        }
    }

    public void postChange(String pageName, String componentId, String fieldName, Object newValue)
    {
        refreshMap();

        Key key = new Key(pageName, componentId, fieldName);

        if (newValue == null)
            persistedValues.remove(key);
        else
        {
            if (!Serializable.class.isInstance(newValue))
                throw new IllegalArgumentException(ServicesMessages
                        .clientStateMustBeSerializable(newValue));

            persistedValues.put(key, newValue);
        }

        clientData = null;
    }

    /**
     * Refreshes the _persistedValues map if it is not up to date.
     */
    @SuppressWarnings("unchecked")
    private void refreshMap()
    {
        if (mapUptoDate) return;

        // Parse the client data to form the map.

        restoreMapFromClientData();

        mapUptoDate = true;
    }

    /**
     * Restores the _persistedValues map from the client data provided in the incoming Request.
     */
    private void restoreMapFromClientData()
    {
        persistedValues.clear();

        if (clientData == null) return;

        ObjectInputStream in = null;

        try
        {
            in = clientDataEncoder.decodeClientData(clientData);

            int count = in.readInt();

            for (int i = 0; i < count; i++)
            {
                Key key = (Key) in.readObject();
                Object value = in.readObject();

                persistedValues.put(key, value);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ServicesMessages.corruptClientState(), ex);
        }
        finally
        {
            InternalUtils.close(in);
        }
    }

    private void refreshClientData()
    {
        // Client data will be null after a change to the map, or if there was no client data in the
        // request. In any other case where the client data is non-null, it is by definition
        // up-to date (since it is reset to null any time there's a change to the map).

        if (clientData != null) return;

        // Very typical: we're refreshing the client data but haven't created the map yet, and there
        // was no value in the request. Leave it as null.

        if (!mapUptoDate) return;

        // Null is also appropriate when the persisted values are empty.

        if (persistedValues.isEmpty()) return;

        // Otherwise, time to update clientData from persistedValues

        ClientDataSink sink = clientDataEncoder.createSink();

        ObjectOutputStream os = sink.getObjectOutputStream();

        try
        {
            os.writeInt(persistedValues.size());

            for (Map.Entry<Key, Object> e : persistedValues.entrySet())
            {
                os.writeObject(e.getKey());
                os.writeObject(e.getValue());
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        finally
        {
            InternalUtils.close(os);
        }

        clientData = sink.getClientData();
    }
}
