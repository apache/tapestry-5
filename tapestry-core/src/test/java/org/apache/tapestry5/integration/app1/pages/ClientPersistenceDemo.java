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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Session;
import org.apache.tapestry5.integration.app1.ClientDataWrapper;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ClientPersistenceDemo
{
    @Persist("client")
    private Object persistedValue;

    @Inject
    private Request request;

    public Object getPersistedValue()
    {
        return persistedValue;
    }

    public boolean getSessionExists()
    {
        return session() != null;
    }

    void onActionFromStoreString()
    {
        persistedValue = "A String";
    }

    void onActionFromStoreComplex()
    {
        persistedValue = new ClientDataWrapper("data inside wrapper");
    }

    void onActionFromStoreBad()
    {
        persistedValue = new Runnable()
        {
            public void run()
            {
            }
        };
    }

    void onActionFromNixSession()
    {
        if (getSessionExists() && !session().isInvalidated())
        {
            session().invalidate();
        }
    }

    private Session session()
    {
        return request.getSession(false);
    }
}
