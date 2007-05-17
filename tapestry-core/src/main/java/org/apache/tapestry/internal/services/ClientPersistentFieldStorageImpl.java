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

import static org.apache.tapestry.ioc.IOCConstants.PERTHREAD_SCOPE;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.tapestry.Link;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.internal.util.Base64ObjectInputStream;
import org.apache.tapestry.internal.util.Base64ObjectOutputStream;
import org.apache.tapestry.ioc.annotations.Scope;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.services.PersistentFieldChange;
import org.apache.tapestry.services.Request;

/**
 * Manages client-persistent values on behalf of a {@link ClientPersistentFieldStorageImpl}. Some
 * effort is made to ensure that we don't uncessarily convert between objects and Base64 (the
 * encoding used to record the value on the client).
 */
@Scope(PERTHREAD_SCOPE)
public class ClientPersistentFieldStorageImpl implements ClientPersistentFieldStorage
{
    static final String PARAMETER_NAME = "t:state:client";

    private static class Key implements Serializable
    {
        private static final long serialVersionUID = -2741540370081645945L;

        private final String _pageName;

        private final String _componentId;

        private final String _fieldName;

        Key(final String pageName, final String componentId, final String fieldName)
        {
            _pageName = pageName;
            _componentId = componentId;
            _fieldName = fieldName;
        }

        public boolean matches(String pageName)
        {
            return _pageName.equals(pageName);
        }

        public PersistentFieldChange toChange(Object value)
        {
            return new PersistentFieldChangeImpl(_componentId == null ? "" : _componentId,
                    _fieldName, value);
        }

        @Override
        public int hashCode()
        {
            final int PRIME = 31;

            int result = 1;

            result = PRIME * result + ((_componentId == null) ? 0 : _componentId.hashCode());

            // _fieldName and _pageName are never null

            result = PRIME * result + _fieldName.hashCode();
            result = PRIME * result + _pageName.hashCode();

            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final Key other = (Key) obj;

            // _fieldName and _pageName are never null

            if (!_fieldName.equals(other._fieldName)) return false;
            if (!_pageName.equals(other._pageName)) return false;

            if (_componentId == null)
            {
                if (other._componentId != null) return false;
            }
            else if (!_componentId.equals(other._componentId)) return false;

            return true;
        }
    }

    private final Map<Key, Object> _persistedValues = newMap();

    private String _clientData;

    private boolean _mapUptoDate = false;

    public ClientPersistentFieldStorageImpl(Request request)
    {
        String value = request.getParameter(PARAMETER_NAME);

        // MIME can encode to a '+' character; the browser converts that to a space; we convert it
        // back.

        _clientData = value == null ? null : value.replace(' ', '+');
    }

    public void updateLink(Link link)
    {
        refreshClientData();

        if (_clientData != null) link.addParameter(PARAMETER_NAME, _clientData);
    }

    public Collection<PersistentFieldChange> gatherFieldChanges(String pageName)
    {
        refreshMap();

        if (_persistedValues.isEmpty()) return Collections.emptyList();

        Collection<PersistentFieldChange> result = CollectionFactory.newList();

        for (Map.Entry<Key, Object> e : _persistedValues.entrySet())
        {
            Key key = e.getKey();

            if (key.matches(pageName)) result.add(key.toChange(e.getValue()));
        }

        return result;
    }

    public void postChange(String pageName, String componentId, String fieldName, Object newValue)
    {
        refreshMap();

        Key key = new Key(pageName, componentId, fieldName);

        if (newValue == null)
            _persistedValues.remove(key);
        else
        {
            if (!Serializable.class.isInstance(newValue))
                throw new IllegalArgumentException(ServicesMessages
                        .clientStateMustBeSerializable(newValue));

            _persistedValues.put(key, newValue);
        }

        _clientData = null;
    }

    @SuppressWarnings("unchecked")
    private void refreshMap()
    {
        if (_mapUptoDate) return;

        // Parse the client data to form the map.

        restoreMapFromClientData();

        _mapUptoDate = true;
    }

    private void restoreMapFromClientData()
    {
        _persistedValues.clear();

        if (_clientData == null) return;

        ObjectInputStream in = null;

        try
        {
            in = new Base64ObjectInputStream(_clientData);

            int count = in.readInt();

            for (int i = 0; i < count; i++)
            {
                Key key = (Key) in.readObject();
                Object value = in.readObject();

                _persistedValues.put(key, value);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ServicesMessages.corruptClientState(), ex);
        }
        finally
        {
            TapestryInternalUtils.close(in);
        }
    }

    private void refreshClientData()
    {
        // Client data will be null after a change to the map, or if there was no client data in the
        // request. In any other case where the client data is non-null, it is by definition
        // up-to date (since it is reset to null any time there's a change to the map).

        if (_clientData != null) return;

        // Very typical: we're refreshing the client data but haven't created the map yet, and there
        // was no value in the request. Leave it as null.

        if (!_mapUptoDate) return;

        // Null is also appropriate when the persisted values are empty.

        if (_persistedValues.isEmpty()) return;

        // Otherwise, time to update _clientData from _persistedValues

        Base64ObjectOutputStream os = null;

        try
        {
            os = new Base64ObjectOutputStream();

            os.writeInt(_persistedValues.size());

            for (Map.Entry<Key, Object> e : _persistedValues.entrySet())
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
            TapestryInternalUtils.close(os);
        }

        _clientData = os.toBase64();
    }
}
