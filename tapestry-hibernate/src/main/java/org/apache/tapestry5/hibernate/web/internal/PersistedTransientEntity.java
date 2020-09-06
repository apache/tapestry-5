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

package org.apache.tapestry5.hibernate.web.internal;

import org.hibernate.Session;

public class PersistedTransientEntity implements SessionRestorable
{
    private final Object transientEntity;

    public PersistedTransientEntity(Object transientEntity)
    {
        assert transientEntity != null;

        this.transientEntity = transientEntity;
    }

    @Override
    public Object restoreWithSession(Session session)
    {
        return transientEntity;
    }

    @Override
    public String toString()
    {
        return String.format("PersistedTransientEntity<%s>", transientEntity);
    }
}
