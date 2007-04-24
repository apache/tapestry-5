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

package org.apache.tapestry.ioc.internal;

import static org.apache.tapestry.ioc.internal.ServiceBuilderMethodInvoker.findParameterizedTypeFromGenericType;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ServiceBuilderMethodInvokerTest extends IOCInternalTestCase
{
    private static final String SERVICE_ID = "ioc.Fie";

    @Test
    public void noargs_method()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        ServiceBuilderResources resources = newServiceCreatorResources();
        Log log = newLog();

        fixture._fie = newFieService();

        trainForConstructor(resources, log);

        train_getModuleBuilder(resources, fixture);

        train_isDebugEnabled(log, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(findMethod(fixture, "build_noargs"),
                resources);

        Object actual = sc.createObject();

        assertSame(actual, fixture._fie);

        verify();
    }

    private void trainForConstructor(ServiceBuilderResources resources, Log log)
    {
        train_getServiceId(resources, SERVICE_ID);

        train_getServiceLog(resources, log);

        train_getServiceInterface(resources, FieService.class);
    }

    @Test
    public void method_with_args()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        Method method = findMethod(fixture, "build_args");
        ServiceBuilderResources resources = newServiceCreatorResources();

        Log log = newLog();

        fixture._expectedServiceId = SERVICE_ID;
        fixture._expectedServiceInterface = FieService.class;
        fixture._expectedServiceResources = resources;
        fixture._expectedLog = log;

        fixture._fie = newFieService();

        trainForConstructor(resources, log);

        train_getModuleBuilder(resources, fixture);

        train_isDebugEnabled(log, true);

        log.debug(IOCMessages.invokingMethod(method));

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(method, resources);

        Object actual = sc.createObject();

        assertSame(actual, fixture._fie);

        verify();
    }

    @Test
    public void injected_service_method()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        ServiceBuilderResources resources = newServiceCreatorResources();
        Log log = newLog();

        fixture._fie = newFieService();
        fixture._expectedFoe = newFoe();

        trainForConstructor(resources, log);

        train_getModuleBuilder(resources, fixture);

        train_getService(resources, "Foe", FoeService.class, fixture._expectedFoe);

        train_isDebugEnabled(log, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(findMethod(fixture, "build_injected"),
                resources);

        Object actual = sc.createObject();

        assertSame(actual, fixture._fie);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void injected_ordered_collection()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        ServiceBuilderResources resources = newServiceCreatorResources();
        Log log = newLog();

        fixture._fie = newFieService();
        List<Runnable> result = newMock(List.class);
        fixture._expectedConfiguration = result;

        trainForConstructor(resources, log);

        train_getModuleBuilder(resources, fixture);

        expect(resources.getOrderedConfiguration(Runnable.class)).andReturn(result);

        train_isDebugEnabled(log, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(findMethod(
                fixture,
                "buildWithOrderedConfiguration"), resources);

        Object actual = sc.createObject();

        assertSame(actual, fixture._fie);

        verify();

    }

    @SuppressWarnings("unchecked")
    @Test
    public void injected_unordered_collection()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        ServiceBuilderResources resources = newServiceCreatorResources();
        Log log = newLog();

        fixture._fie = newFieService();
        Collection<Runnable> result = newMock(Collection.class);
        fixture._expectedConfiguration = result;

        trainForConstructor(resources, log);

        train_getModuleBuilder(resources, fixture);

        expect(resources.getUnorderedConfiguration(Runnable.class)).andReturn(result);

        train_isDebugEnabled(log, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(findMethod(
                fixture,
                "buildWithUnorderedConfiguration"), resources);

        Object actual = sc.createObject();

        assertSame(actual, fixture._fie);

        verify();
    }

    private FoeService newFoe()
    {
        return newFoeService();
    }

    @Test
    public void builder_method_returns_null()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        ServiceBuilderResources resources = newServiceCreatorResources();
        Log log = newLog();

        trainForConstructor(resources, log);

        train_getModuleBuilder(resources, fixture);

        train_isDebugEnabled(log, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(findMethod(fixture, "build_noargs"),
                resources);

        try
        {
            sc.createObject();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            Assert.assertEquals(ex.getMessage(), "Builder method "
                    + ServiceBuilderMethodFixture.class.getName() + ".build_noargs() "
                    + "(for service 'ioc.Fie') returned null.");
        }

        verify();
    }

    @Test
    public void builder_method_failed()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        ServiceBuilderResources resources = newServiceCreatorResources();
        Log log = newLog();

        trainForConstructor(resources, log);

        train_getModuleBuilder(resources, fixture);

        train_isDebugEnabled(log, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(findMethod(fixture, "build_fail"),
                resources);

        try
        {
            sc.createObject();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Error invoking service builder method "
                    + ServiceBuilderMethodFixture.class.getName() + ".build_fail() "
                    + "(for service 'ioc.Fie'): Method failed.");

            Throwable cause = ex.getCause();

            assertEquals(cause.getMessage(), "Method failed.");
        }

        verify();
    }

    @Test
    public void auto_dependency()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        Method method = findMethod(fixture, "build_auto");

        ServiceBuilderResources resources = newServiceCreatorResources();
        Log log = newLog();

        fixture._fie = newFieService();
        fixture._expectedFoe = newFoeService();

        trainForConstructor(resources, log);

        train_getModuleBuilder(resources, fixture);

        train_getService(resources, FoeService.class, fixture._expectedFoe);

        train_isDebugEnabled(log, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(method, resources);

        Object actual = sc.createObject();

        verify();

        assertSame(actual, fixture._fie);
    }

    protected final void train_getModuleBuilder(ServiceBuilderResources resources,
            Object moduleBuilder)
    {
        expect(resources.getModuleBuilder()).andReturn(moduleBuilder);
    }

    @Test
    public void parameterized_type_of_generic_parameter()
    {
        Method m = findMethod(ServiceBuilderMethodFixture.class, "methodWithParameterizedList");

        assertEquals(m.getParameterTypes()[0], List.class);
        Type type = m.getGenericParameterTypes()[0];

        assertEquals(type.toString(), "java.util.List<java.lang.Runnable>");
        assertEquals(findParameterizedTypeFromGenericType(type), Runnable.class);
    }

    @Test
    public void parameterized_type_of_nongeneric_parameter()
    {
        Method m = findMethod(ServiceBuilderMethodFixture.class, "methodWithList");

        assertEquals(m.getParameterTypes()[0], List.class);
        Type type = m.getGenericParameterTypes()[0];

        assertEquals(type.toString(), "interface java.util.List");
        assertEquals(findParameterizedTypeFromGenericType(type), Object.class);
    }

    @Test
    public void parameterize_type_for_non_supported_type()
    {
        Method m = findMethod(ServiceBuilderMethodFixture.class, "methodWithWildcardList");

        assertEquals(m.getParameterTypes()[0], List.class);
        Type type = m.getGenericParameterTypes()[0];

        try
        {
            findParameterizedTypeFromGenericType(type);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), IOCMessages.genericTypeNotSupported(type));
        }
    }

    private FoeService newFoeService()
    {
        return newMock(FoeService.class);
    }

    private FieService newFieService()
    {
        return newMock(FieService.class);
    }

}
