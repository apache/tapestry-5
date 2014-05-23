// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.jpa.integration.app5;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class DummyPersistenceProvider implements PersistenceProvider
{
    @Override
    public EntityManagerFactory createEntityManagerFactory(String emName, Map map)
    {
        return new DummyEntityManagerFactory();
    }

    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map map)
    {
        return new DummyEntityManagerFactory();
    }

    @Override
    public ProviderUtil getProviderUtil()
    {
        return null;
    }

}
