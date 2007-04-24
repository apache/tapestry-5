// Copyright 2006 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.InfrastructureContribution;
import org.apache.tapestry.services.InfrastructureManager;
import org.testng.annotations.Test;

/**
 * 
 */
public class InfrastructureManagerImplTest extends InternalBaseTestCase
{
    @Test
    public void no_conflict()
    {
        Log log = newLog();

        replay();

        Collection<InfrastructureContribution> configuration = buildConfiguration();

        InfrastructureManager manager = new InfrastructureManagerImpl(log, configuration);

        Map<String, Object> map = manager.getContributionsForMode("foo");

        assertEquals(map.size(), 2);
        assertEquals(map.get("fred"), "FRED");
        assertEquals(map.get("barney"), "BARNEY");

        verify();
    }

    @Test
    public void first_entry_wins_on_conflict()
    {
        Log log = newLog();

        log
                .warn("Contribution FRED-CONFLICT (as 'fred') duplicates existing contribution FRED and has been ignored.");

        replay();

        Collection<InfrastructureContribution> configuration = buildConfiguration();
        configuration.add(new InfrastructureContribution("fred", "FRED-CONFLICT"));

        InfrastructureManager manager = new InfrastructureManagerImpl(log, configuration);

        Map<String, Object> map = manager.getContributionsForMode("foo");

        assertEquals(map.size(), 2);
        assertEquals(map.get("fred"), "FRED");
        assertEquals(map.get("barney"), "BARNEY");

        verify();
    }

    @Test
    public void contributions_to_other_modes_are_ignored()
    {
        Log log = newLog();

        replay();

        Collection<InfrastructureContribution> configuration = buildConfiguration();

        configuration.add(new InfrastructureContribution("barney", "bar", "BARNEY2"));

        InfrastructureManager manager = new InfrastructureManagerImpl(log, configuration);

        Map<String, Object> map = manager.getContributionsForMode("foo");

        assertEquals(map.size(), 2);
        assertEquals(map.get("fred"), "FRED");
        assertEquals(map.get("barney"), "BARNEY");

        verify();
    }

    @Test
    public void mode_specific_contribution_overrides_general_contribution()
    {
        Log log = newLog();

        replay();

        Collection<InfrastructureContribution> configuration = buildConfiguration();

        configuration.add(new InfrastructureContribution("fred", "foo", "FRED-OVERRIDE"));

        InfrastructureManager manager = new InfrastructureManagerImpl(log, configuration);

        Map<String, Object> map = manager.getContributionsForMode("foo");

        assertEquals(map.size(), 2);
        assertEquals(map.get("fred"), "FRED-OVERRIDE");
        assertEquals(map.get("barney"), "BARNEY");

        verify();

    }

    private Collection<InfrastructureContribution> buildConfiguration()
    {
        Collection<InfrastructureContribution> configuration = newList();

        configuration.add(new InfrastructureContribution("fred", "FRED"));
        configuration.add(new InfrastructureContribution("barney", "foo", "BARNEY"));

        return configuration;
    }
}
