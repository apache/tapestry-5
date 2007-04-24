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

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;

import java.util.Collections;
import java.util.Map;

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.services.ApplicationStateContribution;
import org.apache.tapestry.services.ApplicationStateCreator;
import org.apache.tapestry.services.ApplicationStateManager;
import org.apache.tapestry.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry.services.ApplicationStatePersistenceStrategySource;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.annotations.Test;

public class ApplicationStateManagerImplTest extends InternalBaseTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void get_from_configured_aso()
    {
        String strategyName = "ethereal";
        ApplicationStatePersistenceStrategy strategy = newApplicationStatePersistenceStrategy();
        ApplicationStatePersistenceStrategySource source = newApplicationStatePersistenceStrategySource();
        Class asoClass = ReadOnlyBean.class;
        ApplicationStateCreator<ReadOnlyBean> creator = newApplicationStateCreator();
        ReadOnlyBean aso = new ReadOnlyBean();

        Map<Class, ApplicationStateContribution> configuration = Collections.singletonMap(
                asoClass,
                new ApplicationStateContribution(strategyName, creator));

        train_get(source, strategyName, strategy);

        train_get(strategy, asoClass, creator, aso);

        replay();

        ApplicationStateManager manager = new ApplicationStateManagerImpl(configuration, source);

        assertSame(manager.get(asoClass), aso);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void set_configured_aso()
    {
        String strategyName = "ethereal";
        ApplicationStatePersistenceStrategy strategy = newApplicationStatePersistenceStrategy();
        ApplicationStatePersistenceStrategySource source = newApplicationStatePersistenceStrategySource();
        Class asoClass = ReadOnlyBean.class;
        Object aso = new ReadOnlyBean();

        Map<Class, ApplicationStateContribution> configuration = Collections.singletonMap(
                asoClass,
                new ApplicationStateContribution(strategyName));

        train_get(source, strategyName, strategy);

        strategy.set(asoClass, aso);

        replay();

        ApplicationStateManager manager = new ApplicationStateManagerImpl(configuration, source);

        manager.set(asoClass, aso);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void get_from_unconfigured_aso()
    {
        ApplicationStatePersistenceStrategy strategy = newApplicationStatePersistenceStrategy();
        ApplicationStatePersistenceStrategySource source = newApplicationStatePersistenceStrategySource();
        Class asoClass = ReadOnlyBean.class;
        final Holder holder = new Holder();

        train_get(source, ApplicationStateManagerImpl.DEFAULT_STRATEGY, strategy);

        IAnswer answer = new IAnswer()
        {
            public Object answer() throws Throwable
            {
                ApplicationStateCreator creator = (ApplicationStateCreator) EasyMock
                        .getCurrentArguments()[1];

                Object aso = creator.create();

                holder.put(aso);

                return aso;
            }
        };

        expect(strategy.get(eq(asoClass), isA(ApplicationStateCreator.class))).andAnswer(answer);

        replay();

        Map<Class, ApplicationStateContribution> configuration = Collections.emptyMap();

        ApplicationStateManager manager = new ApplicationStateManagerImpl(configuration, source);

        Object actual = manager.get(asoClass);

        assertSame(actual, holder.get());

        verify();
    }
}
