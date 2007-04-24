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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.Collections;
import java.util.Map;

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.services.Alias;
import org.apache.tapestry.services.AliasManager;
import org.testng.annotations.Test;

public class AliasImplTest extends InternalBaseTestCase
{
    private Map<String, Object> _emptyMap = Collections.emptyMap();

    @Test
    public void mode_not_set_when_resolution_requested()
    {
        AliasManager manager = newAliasManager();
        AliasManager overridesManager = newAliasManager();
        ServiceLocator locator = newServiceLocator();

        replay();

        Alias alias = new AliasImpl(manager, overridesManager);

        // Do not assume that alias and provider are the same;
        // that's an implementation choice.

        ObjectProvider provider = alias.getObjectProvider();

        try
        {
            provider.provide("expression", Runnable.class, locator);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), ServicesMessages.aliasModeNotSet());
        }

        verify();
    }

    protected final AliasManager newAliasManager()
    {
        return newMock(AliasManager.class);
    }

    @Test
    public void resolve_object_within_mode()
    {
        String property = "myrunnable";
        String mode = "papyrus";

        AliasManager manager = newAliasManager();
        AliasManager overridesManager = newAliasManager();

        ServiceLocator locator = newServiceLocator();
        Runnable r = newRunnable();

        Map<String, Object> configuration = newMap();
        configuration.put(property, r);

        getAliasesForMode(manager, mode, configuration);
        getAliasesForMode(overridesManager, mode, _emptyMap);

        replay();

        Alias alias = new AliasImpl(manager, overridesManager);

        alias.setMode(mode);

        // Do not assume that infra and provider are the same;
        // that's an implementation choice.

        ObjectProvider provider = alias.getObjectProvider();

        Runnable actual = provider.provide(property, Runnable.class, locator);

        assertSame(actual, r);

        verify();
    }

    @Test
    public void overrides_manager_has_precendence()
    {
        String property = "myrunnable";
        String mode = "papyrus";

        AliasManager manager = newAliasManager();
        AliasManager overridesManager = newAliasManager();

        ServiceLocator locator = newServiceLocator();
        Runnable masterRunnable = newRunnable();
        Runnable overrideRunnable = newRunnable();

        Map<String, Object> masterConfiguration = newMap();
        masterConfiguration.put(property, masterRunnable);

        Map<String, Object> overrideConfiguration = newMap();
        overrideConfiguration.put(property, overrideRunnable);

        getAliasesForMode(manager, mode, masterConfiguration);
        getAliasesForMode(overridesManager, mode, overrideConfiguration);

        replay();

        Alias alias = new AliasImpl(manager, overridesManager);

        alias.setMode(mode);

        ObjectProvider provider = alias.getObjectProvider();

        Runnable actual = provider.provide(property, Runnable.class, locator);

        assertSame(actual, overrideRunnable);

        verify();
    }

    /** Check that the manager is only consulted once. */
    @Test
    public void configuration_is_cached()
    {
        String property = "myrunnable";
        String mode = "clay";

        AliasManager manager = newAliasManager();
        AliasManager overridesManager = newAliasManager();
        ServiceLocator locator = newServiceLocator();
        Runnable r = newRunnable();

        Map<String, Object> configuration = newMap();
        configuration.put(property, r);

        getAliasesForMode(manager, mode, configuration);
        getAliasesForMode(overridesManager, mode, _emptyMap);

        replay();

        Alias alias = new AliasImpl(manager, overridesManager);

        alias.setMode(mode);

        // Do not assume that infra and provider are the same;
        // that's an implementation choice.

        ObjectProvider provider = alias.getObjectProvider();

        Runnable actual1 = provider.provide(property, Runnable.class, locator);
        Runnable actual2 = provider.provide(property, Runnable.class, locator);

        assertSame(actual1, r);
        assertSame(actual2, r);

        verify();
    }

    @Test
    public void expression_not_found_in_configuration()
    {
        String mode = "clay";

        AliasManager manager = newAliasManager();
        AliasManager overridesManager = newAliasManager();
        ServiceLocator locator = newServiceLocator();

        Map<String, Object> configuration = newMap();

        configuration.put("fred", this);
        configuration.put("barney", this);
        configuration.put("wilma", this);

        getAliasesForMode(manager, mode, configuration);
        getAliasesForMode(overridesManager, mode, _emptyMap);

        replay();

        Alias alias = new AliasImpl(manager, overridesManager);

        alias.setMode(mode);

        ObjectProvider provider = alias.getObjectProvider();

        try
        {
            provider.provide("someexpression", Runnable.class, locator);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "No alias property 'someexpression' has been configured. Configured properties are: barney, fred, wilma.");
        }

        verify();

    }

    @Test
    public void requested_type_not_compatible_with_object()
    {
        String property = "myrunnable";
        String mode = "papyrus";

        AliasManager manager = newAliasManager();
        AliasManager overridesManager = newAliasManager();
        ServiceLocator locator = newServiceLocator();
        Runnable r = newRunnable();

        Map<String, Object> configuration = newMap();
        configuration.put(property, r);

        getAliasesForMode(manager, mode, configuration);
        getAliasesForMode(overridesManager, mode, _emptyMap);

        replay();

        Alias alias = new AliasImpl(manager, overridesManager);

        alias.setMode(mode);

        // Do not assume that infra and provider are the same;
        // that's an implementation choice.

        ObjectProvider provider = alias.getObjectProvider();

        try
        {
            provider.provide(property, UpdateListenerHub.class, locator);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), ServicesMessages.aliasPropertyWrongType(
                    property,
                    r,
                    UpdateListenerHub.class));
        }

        verify();

    }
}
