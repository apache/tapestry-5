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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry5.services.Alias;
import org.apache.tapestry5.services.AliasManager;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

public class AliasImplTest extends InternalBaseTestCase
{
    private Map<Class, Object> emptyMap = Collections.emptyMap();

    protected final AliasManager newAliasManager()
    {
        return newMock(AliasManager.class);
    }

    @Test
    public void resolve_object_within_mode()
    {
        String mode = "papyrus";

        AliasManager manager = newAliasManager();
        AliasManager overridesManager = newAliasManager();
        AnnotationProvider annotationProvider = mockAnnotationProvider();

        ObjectLocator locator = mockObjectLocator();
        Runnable r = mockRunnable();

        Map<Class, Object> configuration = newMap();
        configuration.put(Runnable.class, r);

        train_getAliasesForMode(manager, mode, configuration);
        train_getAliasesForMode(overridesManager, mode, emptyMap);

        replay();

        Alias alias = new AliasImpl(manager, mode, overridesManager);

        // Do not assume that infra and provider are the same;
        // that's an implementation choice.

        ObjectProvider provider = alias.getObjectProvider();

        // Run through a couple of times to ensure that values are cached.
        for (int i = 0; i < 2; i++)
        {
            Runnable actual = provider.provide(Runnable.class, annotationProvider, locator);

            assertSame(actual, r);
        }

        verify();
    }

    @Test
    public void overrides_manager_has_precendence()
    {
        String mode = "papyrus";

        AliasManager manager = newAliasManager();
        AliasManager overridesManager = newAliasManager();
        AnnotationProvider annotationProvider = mockAnnotationProvider();

        ObjectLocator locator = mockObjectLocator();
        Runnable r = mockRunnable();
        Runnable override = mockRunnable();

        Map<Class, Object> configuration = newMap();
        configuration.put(Runnable.class, r);

        train_getAliasesForMode(manager, mode, configuration);

        Map<Class, Object> overrideConfiguration = newMap();
        configuration.put(Runnable.class, override);

        train_getAliasesForMode(overridesManager, mode, overrideConfiguration);

        replay();

        Alias alias = new AliasImpl(manager, mode, overridesManager);

        ObjectProvider provider = alias.getObjectProvider();

        Runnable actual = provider.provide(Runnable.class, annotationProvider, locator);

        assertSame(actual, override);

        verify();
    }

    @Test
    public void type_not_found_in_configuration()
    {
        String mode = "papyrus";

        AliasManager manager = newAliasManager();
        AliasManager overridesManager = newAliasManager();
        AnnotationProvider annotationProvider = mockAnnotationProvider();

        ObjectLocator locator = mockObjectLocator();

        train_getAliasesForMode(manager, mode, emptyMap);
        train_getAliasesForMode(overridesManager, mode, emptyMap);

        replay();

        Alias alias = new AliasImpl(manager, mode, overridesManager);

        // Do not assume that infra and provider are the same;
        // that's an implementation choice.

        ObjectProvider provider = alias.getObjectProvider();

        assertNull(provider.provide(Runnable.class, annotationProvider, locator));

        verify();

    }
}
