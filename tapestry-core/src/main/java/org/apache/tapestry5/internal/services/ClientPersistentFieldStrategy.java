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
import org.apache.tapestry5.services.LinkCreationListener;
import org.apache.tapestry5.services.PersistentFieldChange;
import org.apache.tapestry5.services.PersistentFieldStrategy;

import java.util.Collection;

/**
 * Implements simple client-persistent properties. Most of the logic is delegated to an instance of {@link
 * ClientPersistentFieldStorage}. This division of layer allows this service to be a true singleton, and a listener to
 * the {@link LinkSource}, and allow per-request state to be isolated inside the other service.
 */
public class ClientPersistentFieldStrategy implements PersistentFieldStrategy, LinkCreationListener
{
    private final ClientPersistentFieldStorage storage;

    public ClientPersistentFieldStrategy(ClientPersistentFieldStorage storage)
    {
        this.storage = storage;
    }

    public Collection<PersistentFieldChange> gatherFieldChanges(String pageName)
    {
        return storage.gatherFieldChanges(pageName);
    }

    public void postChange(String pageName, String componentId, String fieldName, Object newValue)
    {
        storage.postChange(pageName, componentId, fieldName, newValue);
    }

    public void createdComponentEventLink(Link link)
    {
        storage.updateLink(link);
    }

    public void createdPageRenderLink(Link link)
    {
        storage.updateLink(link);
    }

    public void discardChanges(String pageName)
    {
        storage.discardChanges(pageName);
    }
}
