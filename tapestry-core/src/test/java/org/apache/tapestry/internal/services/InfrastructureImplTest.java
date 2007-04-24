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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.Map;

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.services.Infrastructure;
import org.apache.tapestry.services.InfrastructureManager;
import org.testng.annotations.Test;

/**
 * 
 */
public class InfrastructureImplTest extends InternalBaseTestCase
{
    @Test
    public void mode_not_set_when_resolution_requested()
    {
        InfrastructureManager manager = newInfrastructureManager();
        ServiceLocator locator = newServiceLocator();

        replay();

        Infrastructure infra = new InfrastructureImpl(manager);

        // Do not assume that infra and provider are the same;
        // that's an implementation choice.

        ObjectProvider provider = infra.getObjectProvider();

        try
        {
            provider.provide("expression", Runnable.class, locator);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), ServicesMessages.infrastructureModeNotSet());
        }

        verify();
    }

    protected final InfrastructureManager newInfrastructureManager()
    {
        return newMock(InfrastructureManager.class);
    }

    @Test
    public void resolve_object_within_mode()
    {
        InfrastructureManager manager = newInfrastructureManager();
        ServiceLocator locator = newServiceLocator();
        Runnable r = newRunnable();

        Map<String, Object> configuration = newMap();
        configuration.put("myrunnable", r);

        train_getContributionsForMode(manager, "papyrus", configuration);

        replay();

        Infrastructure infra = new InfrastructureImpl(manager);

        infra.setMode("papyrus");

        // Do not assume that infra and provider are the same;
        // that's an implementation choice.

        ObjectProvider provider = infra.getObjectProvider();

        Runnable actual = provider.provide("myrunnable", Runnable.class, locator);

        assertSame(actual, r);

        verify();
    }

    /** Check that the manager is only consulted once. */
    @Test
    public void configuration_is_cached()
    {
        InfrastructureManager manager = newInfrastructureManager();
        ServiceLocator locator = newServiceLocator();
        Runnable r = newRunnable();

        Map<String, Object> configuration = newMap();
        configuration.put("myrunnable", r);

        train_getContributionsForMode(manager, "clay", configuration);

        replay();

        Infrastructure infra = new InfrastructureImpl(manager);

        infra.setMode("clay");

        // Do not assume that infra and provider are the same;
        // that's an implementation choice.

        ObjectProvider provider = infra.getObjectProvider();

        Runnable actual1 = provider.provide("myrunnable", Runnable.class, locator);
        Runnable actual2 = provider.provide("myrunnable", Runnable.class, locator);

        assertSame(actual1, r);
        assertSame(actual2, r);

        verify();
    }

    @Test
    public void expression_not_found_in_configuration()
    {
        InfrastructureManager manager = newInfrastructureManager();
        ServiceLocator locator = newServiceLocator();

        Map<String, Object> configuration = newMap();

        configuration.put("fred", this);
        configuration.put("barney", this);
        configuration.put("wilma", this);

        train_getContributionsForMode(manager, "clay", configuration);

        replay();

        Infrastructure infra = new InfrastructureImpl(manager);

        infra.setMode("clay");

        ObjectProvider provider = infra.getObjectProvider();

        try
        {
            provider.provide("someexpression", Runnable.class, locator);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "No infrastructure property 'someexpression' has been configured. Configured properties are: barney, fred, wilma.");
        }

        verify();

    }

    @Test
    public void requested_type_not_compatible_with_object()
    {
        InfrastructureManager manager = newInfrastructureManager();
        ServiceLocator locator = newServiceLocator();
        Runnable r = newRunnable();

        Map<String, Object> configuration = newMap();
        configuration.put("myrunnable", r);

        train_getContributionsForMode(manager, "papyrus", configuration);

        replay();

        Infrastructure infra = new InfrastructureImpl(manager);

        infra.setMode("papyrus");

        // Do not assume that infra and provider are the same;
        // that's an implementation choice.

        ObjectProvider provider = infra.getObjectProvider();

        try
        {
            provider.provide("myrunnable", UpdateListenerHub.class, locator);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), ServicesMessages.infrastructurePropertyWrongType(
                    "myrunnable",
                    r,
                    UpdateListenerHub.class));
        }

        verify();

    }

    protected final void train_getContributionsForMode(InfrastructureManager manager, String mode,
            Map<String, Object> configuration)
    {
        expect(manager.getContributionsForMode(mode)).andReturn(configuration);
    }
}
