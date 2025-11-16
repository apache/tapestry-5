// Copyright 2025 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.junit;

import java.lang.reflect.Field;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

public class TapestryIOCJUnitExtension implements BeforeAllCallback, AfterAllCallback,
        BeforeEachCallback, AfterEachCallback, TestInstanceFactory
{

    private TestRegistryManager registryManager;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception
    {
        Class<?> testClass = context.getRequiredTestClass();
        registryManager = new TestRegistryManager(testClass);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception
    {
        if (registryManager != null)
        {
            registryManager.afterTestClass();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception
    {
        // No-op: lifecycle handled by createTestInstance
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception
    {
        if (registryManager != null)
        {
            registryManager.afterTestMethod();
        }
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext,
            ExtensionContext extensionContext) throws TestInstantiationException
    {
        if (registryManager == null)
        {
            registryManager = new TestRegistryManager(factoryContext.getTestClass());
        }
        try
        {
            Registry registry = registryManager.getOrCreateRegistry();
            Object testInstance = registry.autobuild(factoryContext.getTestClass());
            injectFields(testInstance, registry);
            return testInstance;
        }
        catch (Exception e)
        {
            throw new TestInstantiationException("", e);
        }
    }

    private void injectFields(Object instance, Registry registry)
    {
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields())
        {
            if (field.isAnnotationPresent(Inject.class))
            {
                Object service = registry.getObject(field.getType(), null);
                field.setAccessible(true);
                try
                {
                    field.set(instance, service);
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException("Failed to inject field: " + field, e);
                }
            }
        }
    }
}
