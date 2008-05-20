// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.def.ContributionDef;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class ValidatingMappedConfigurationWrapperTest extends IOCInternalTestCase
{
    private static final String SERVICE_ID = "Baz";

    @Test
    public void proper_key_and_value()
    {
        ContributionDef def = mockContributionDef();
        Logger logger = mockLogger();
        Map<Class, ContributionDef> keyToContribution = newMap();
        MappedConfiguration<Class, Runnable> delegate = mockMappedConfiguration();

        Class key = Integer.class;
        Runnable value = mockRunnable();

        delegate.add(key, value);

        replay();

        MappedConfiguration<Class, Runnable> wrapper = new ValidatingMappedConfigurationWrapper<Class, Runnable>(
                SERVICE_ID, def, logger, Class.class, Runnable.class, keyToContribution, delegate);

        wrapper.add(key, value);

        verify();

        assertSame(keyToContribution.get(Integer.class), def);
    }

    @Test
    public void duplicate_key()
    {
        ContributionDef def1 = newContributionDef("contributionPlaceholder1");
        ContributionDef def2 = newContributionDef("contributionPlaceholder2");
        Logger logger = mockLogger();
        Map<Class, ContributionDef> keyToContribution = newMap();

        keyToContribution.put(Integer.class, def1);

        MappedConfiguration<Class, Runnable> delegate = mockMappedConfiguration();

        Class key = Integer.class;
        Runnable value = mockRunnable();

        logger.warn(IOCMessages.contributionDuplicateKey(SERVICE_ID, def2, def1));

        replay();

        MappedConfiguration<Class, Runnable> wrapper = new ValidatingMappedConfigurationWrapper<Class, Runnable>(
                SERVICE_ID, def2, logger, Class.class, Runnable.class, keyToContribution, delegate);

        wrapper.add(key, value);

        verify();

        assertSame(keyToContribution.get(Integer.class), def1);
    }

    @Test
    public void null_key()
    {
        ContributionDef def = newContributionDef("contributionPlaceholder1");
        Logger logger = mockLogger();
        Map<Class, ContributionDef> keyToContribution = newMap();
        MappedConfiguration<Class, Runnable> delegate = mockMappedConfiguration();
        Runnable value = mockRunnable();

        logger.warn(IOCMessages.contributionKeyWasNull(SERVICE_ID, def));

        replay();

        MappedConfiguration<Class, Runnable> wrapper = new ValidatingMappedConfigurationWrapper<Class, Runnable>(
                SERVICE_ID, def, logger, Class.class, Runnable.class, keyToContribution, delegate);

        wrapper.add(null, value);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void wrong_key_type()
    {
        ContributionDef def = newContributionDef("contributionPlaceholder1");
        Logger logger = mockLogger();
        Map<?, ContributionDef> keyToContribution = newMap();
        MappedConfiguration delegate = mockMappedConfiguration();
        Runnable value = mockRunnable();

        logger.warn(IOCMessages
                .contributionWrongKeyType(SERVICE_ID, def, String.class, Class.class));

        replay();

        MappedConfiguration wrapper = new ValidatingMappedConfigurationWrapper(SERVICE_ID, def,
                                                                               logger, Class.class, Runnable.class,
                                                                               keyToContribution, delegate);

        wrapper.add("java.util.List", value);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void wrong_value_type()
    {
        ContributionDef def = newContributionDef("contributionPlaceholder1");
        Logger logger = mockLogger();
        Map<?, ContributionDef> keyToContribution = newMap();
        MappedConfiguration delegate = mockMappedConfiguration();

        logger.warn(IOCMessages.contributionWrongValueType(
                SERVICE_ID,
                def,
                String.class,
                Runnable.class));

        replay();

        MappedConfiguration wrapper = new ValidatingMappedConfigurationWrapper(SERVICE_ID, def,
                                                                               logger, Class.class, Runnable.class,
                                                                               keyToContribution, delegate);

        wrapper.add(List.class, "do something");

        verify();
    }

    @Test
    public void null_value()
    {
        ContributionDef def = newContributionDef("contributionPlaceholder1");
        Logger logger = mockLogger();
        Map<Class, ContributionDef> keyToContribution = newMap();
        MappedConfiguration<Class, Runnable> delegate = mockMappedConfiguration();

        logger.warn(IOCMessages.contributionWasNull(SERVICE_ID, def));

        replay();

        MappedConfiguration<Class, Runnable> wrapper = new ValidatingMappedConfigurationWrapper<Class, Runnable>(
                SERVICE_ID, def, logger, Class.class, Runnable.class, keyToContribution, delegate);

        wrapper.add(Integer.class, null);

        verify();

    }

    private ContributionDef newContributionDef(String methodName)
    {
        return new ContributionDefImpl(SERVICE_ID, findMethod(methodName), getClassFactory());
    }

    public void contributionPlaceholder1()
    {

    }

    public void contributionPlaceholder2()
    {

    }

}
