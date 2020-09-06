// Copyright 2007, 2008, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.transform.pages.ReadOnlyBean;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.services.ApplicationStateContribution;
import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategy;
import org.apache.tapestry5.services.ApplicationStatePersistenceStrategySource;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;
import org.easymock.IAnswer;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

public class ApplicationStateManagerImplTest extends InternalBaseTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void get_from_configured_aso()
    {
        String strategyName = "ethereal";
        ApplicationStatePersistenceStrategy strategy = mockApplicationStatePersistenceStrategy();
        ApplicationStatePersistenceStrategySource source = mockApplicationStatePersistenceStrategySource();
        Class asoClass = ReadOnlyBean.class;
        ApplicationStateCreator<ReadOnlyBean> creator = mockApplicationStateCreator();
        ReadOnlyBean aso = new ReadOnlyBean();

        Map<Class, ApplicationStateContribution> configuration = Collections.singletonMap(asoClass,
                new ApplicationStateContribution(strategyName, creator));

        train_get(source, strategyName, strategy);

        train_get(strategy, asoClass, creator, aso);

        replay();

        ApplicationStateManager manager = new ApplicationStateManagerImpl(configuration, source,
                null);

        assertSame(manager.get(asoClass), aso);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void check_exists_when_null()
    {
        String strategyName = "ethereal";
        ApplicationStatePersistenceStrategy strategy = mockApplicationStatePersistenceStrategy();
        ApplicationStatePersistenceStrategySource source = mockApplicationStatePersistenceStrategySource();
        Class asoClass = ReadOnlyBean.class;
        ApplicationStateCreator<ReadOnlyBean> creator = mockApplicationStateCreator();

        Map<Class, ApplicationStateContribution> configuration = Collections.singletonMap(asoClass,
                new ApplicationStateContribution(strategyName, creator));

        train_get(source, strategyName, strategy);
        train_exists(strategy, asoClass, false);

        replay();

        ApplicationStateManager manager = new ApplicationStateManagerImpl(configuration, source,
                null);

        assertFalse(manager.exists(asoClass));

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void check_exists_when_true()
    {
        String strategyName = "ethereal";
        ApplicationStatePersistenceStrategy strategy = mockApplicationStatePersistenceStrategy();
        ApplicationStatePersistenceStrategySource source = mockApplicationStatePersistenceStrategySource();
        Class asoClass = ReadOnlyBean.class;
        ApplicationStateCreator<ReadOnlyBean> creator = mockApplicationStateCreator();

        Map<Class, ApplicationStateContribution> configuration = Collections.singletonMap(asoClass,
                new ApplicationStateContribution(strategyName, creator));

        train_get(source, strategyName, strategy);
        train_exists(strategy, asoClass, true);

        replay();

        ApplicationStateManager manager = new ApplicationStateManagerImpl(configuration, source,
                null);

        assertTrue(manager.exists(asoClass));

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void set_configured_aso()
    {
        String strategyName = "ethereal";
        ApplicationStatePersistenceStrategy strategy = mockApplicationStatePersistenceStrategy();
        ApplicationStatePersistenceStrategySource source = mockApplicationStatePersistenceStrategySource();
        Class asoClass = ReadOnlyBean.class;
        Object aso = new ReadOnlyBean();

        Map<Class, ApplicationStateContribution> configuration = Collections.singletonMap(asoClass,
                new ApplicationStateContribution(strategyName));

        train_get(source, strategyName, strategy);

        strategy.set(asoClass, aso);

        replay();

        ApplicationStateManager manager = new ApplicationStateManagerImpl(configuration, source,
                null);

        manager.set(asoClass, aso);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void get_from_unconfigured_aso()
    {
        ApplicationStatePersistenceStrategy strategy = mockApplicationStatePersistenceStrategy();
        ApplicationStatePersistenceStrategySource source = mockApplicationStatePersistenceStrategySource();
        Class asoClass = ReadOnlyBean.class;
        final Holder holder = new Holder();
        ObjectLocator locator = mockObjectLocator();

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

        expect(locator.autobuild(EasyMock.isA(String.class), EasyMock.eq(asoClass))).andReturn(
                new ReadOnlyBean());

        replay();

        Map<Class, ApplicationStateContribution> configuration = Collections.emptyMap();

        ApplicationStateManager manager = new ApplicationStateManagerImpl(configuration, source,
                locator);

        Object actual = manager.get(asoClass);

        assertSame(actual, holder.get());

        verify();
    }

    @Test
    public void get_if_exists_and_it_doesnt()
    {
        String strategyName = "ethereal";
        ApplicationStatePersistenceStrategy strategy = mockApplicationStatePersistenceStrategy();
        ApplicationStatePersistenceStrategySource source = mockApplicationStatePersistenceStrategySource();
        Class asoClass = ReadOnlyBean.class;
        ApplicationStateCreator<ReadOnlyBean> creator = mockApplicationStateCreator();

        Map<Class, ApplicationStateContribution> configuration = Collections.singletonMap(asoClass,
                new ApplicationStateContribution(strategyName, creator));

        train_get(source, strategyName, strategy);
        train_getIfExists(strategy, asoClass, null);

        replay();

        ApplicationStateManager manager = new ApplicationStateManagerImpl(configuration, source,
                null);

        assertNull(manager.getIfExists(asoClass));

        verify();
    }

    @Test
    public void get_if_exists_when_it_does_exist()
    {
        String strategyName = "ethereal";
        ApplicationStatePersistenceStrategy strategy = mockApplicationStatePersistenceStrategy();
        ApplicationStatePersistenceStrategySource source = mockApplicationStatePersistenceStrategySource();
        Class asoClass = ReadOnlyBean.class;
        ApplicationStateCreator<ReadOnlyBean> creator = mockApplicationStateCreator();
        ReadOnlyBean aso = new ReadOnlyBean();

        Map<Class, ApplicationStateContribution> configuration = Collections.singletonMap(asoClass,
                new ApplicationStateContribution(strategyName, creator));

        train_get(source, strategyName, strategy);
        train_getIfExists(strategy, asoClass, aso);

        replay();

        ApplicationStateManager manager = new ApplicationStateManagerImpl(configuration, source,
                null);

        assertSame(manager.getIfExists(asoClass), aso);

        verify();
    }
}
