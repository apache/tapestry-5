// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import static org.apache.tapestry5.ioc.internal.AbstractServiceCreator.findParameterizedTypeFromGenericType;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;
import org.slf4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public class ServiceBuilderMethodInvokerTest extends IOCInternalTestCase
{
    private static final String SERVICE_ID = "Fie";

    private static final String CREATOR_DESCRIPTION = "{CREATOR DESCRIPTION}";

    private final OperationTracker tracker = new QuietOperationTracker();

    @Test
    public void noargs_method()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        ServiceBuilderResources resources = mockServiceBuilderResources(tracker);
        Logger logger = mockLogger();

        fixture.fie = mockFieService();

        trainForConstructor(resources, logger);

        train_getModuleBuilder(resources, fixture);

        train_isDebugEnabled(logger, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(resources, CREATOR_DESCRIPTION,
                                                           findMethod(fixture, "build_noargs"));

        Object actual = sc.createObject();

        assertSame(actual, fixture.fie);

        verify();
    }

    private void trainForConstructor(ServiceBuilderResources resources, Logger logger)
    {
        train_getServiceId(resources, SERVICE_ID);

        train_getLogger(resources, logger);

        train_getServiceInterface(resources, FieService.class);
    }

    @Test
    public void method_with_args()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        Method method = findMethod(fixture, "build_args");
        ServiceBuilderResources resources = mockServiceBuilderResources(tracker);

        Logger logger = mockLogger();

        fixture.expectedServiceId = SERVICE_ID;
        fixture.expectedServiceInterface = FieService.class;
        fixture.expectedServiceResources = resources;
        fixture.expectedLogger = logger;

        fixture.fie = mockFieService();

        trainForConstructor(resources, logger);

        train_getModuleBuilder(resources, fixture);

        train_isDebugEnabled(logger, true);

        logger.debug(IOCMessages.invokingMethod(CREATOR_DESCRIPTION));

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(resources, CREATOR_DESCRIPTION, method);

        Object actual = sc.createObject();

        assertSame(actual, fixture.fie);

        verify();
    }

    @Test
    public void inject_annotation_bypasses_resources()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        Method method = findMethod(fixture, "build_with_forced_injection");
        ServiceBuilderResources resources = mockServiceBuilderResources(tracker);

        Logger logger = mockLogger();

        fixture.expectedString = "Injected";

        fixture.fie = mockFieService();

        trainForConstructor(resources, logger);

        train_getModuleBuilder(resources, fixture);

        train_isDebugEnabled(logger, true);

        logger.debug(IOCMessages.invokingMethod(CREATOR_DESCRIPTION));

        // This simulates what the real stack does when it sees @Value("Injected")

        expect(resources.getObject(eq(String.class), isA(AnnotationProvider.class))).andReturn(
                "Injected");

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(resources, CREATOR_DESCRIPTION, method);

        Object actual = sc.createObject();

        assertSame(actual, fixture.fie);

        verify();
    }

    @Test
    public void injected_service_method()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        ServiceBuilderResources resources = mockServiceBuilderResources(tracker);
        Logger logger = mockLogger();

        fixture.fie = mockFieService();
        fixture.expectedFoe = newFoe();

        trainForConstructor(resources, logger);

        train_getModuleBuilder(resources, fixture);

        train_getService(resources, "Foe", FoeService.class, fixture.expectedFoe);

        train_isDebugEnabled(logger, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(resources, CREATOR_DESCRIPTION,
                                                           findMethod(fixture, "build_injected"));

        Object actual = sc.createObject();

        assertSame(actual, fixture.fie);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void injected_ordered_collection()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        ServiceBuilderResources resources = mockServiceBuilderResources(tracker);
        Logger logger = mockLogger();

        fixture.fie = mockFieService();
        List<Runnable> result = newMock(List.class);
        fixture.expectedConfiguration = result;

        trainForConstructor(resources, logger);

        train_getModuleBuilder(resources, fixture);

        expect(resources.getOrderedConfiguration(Runnable.class)).andReturn(result);

        train_isDebugEnabled(logger, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(resources, CREATOR_DESCRIPTION,
                                                           findMethod(fixture, "buildWithOrderedConfiguration"));

        Object actual = sc.createObject();

        assertSame(actual, fixture.fie);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void injected_unordered_collection()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        ServiceBuilderResources resources = mockServiceBuilderResources(tracker);
        Logger logger = mockLogger();

        fixture.fie = mockFieService();
        Collection<Runnable> result = newMock(Collection.class);
        fixture.expectedConfiguration = result;

        trainForConstructor(resources, logger);

        train_getModuleBuilder(resources, fixture);

        expect(resources.getUnorderedConfiguration(Runnable.class)).andReturn(result);

        train_isDebugEnabled(logger, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(resources, CREATOR_DESCRIPTION,
                                                           findMethod(fixture, "buildWithUnorderedConfiguration"));

        Object actual = sc.createObject();

        assertSame(actual, fixture.fie);

        verify();
    }

    private FoeService newFoe()
    {
        return mockFoeService();
    }

    @Test
    public void builder_method_returns_null()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        ServiceBuilderResources resources = mockServiceBuilderResources(tracker);
        Logger logger = mockLogger();

        Method method = findMethod(fixture, "build_noargs");

        trainForConstructor(resources, logger);

        train_getModuleBuilder(resources, fixture);

        train_isDebugEnabled(logger, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(resources, CREATOR_DESCRIPTION, method);

        try
        {
            sc.createObject();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            Assert.assertEquals(ex.getMessage(), "Builder method " + CREATOR_DESCRIPTION
                    + " (for service 'Fie') returned null.");
        }

        verify();
    }

    @Test
    public void builder_method_failed()
    {
        ServiceBuilderMethodFixture fixture = new ServiceBuilderMethodFixture();
        ServiceBuilderResources resources = mockServiceBuilderResources(tracker);
        Logger logger = mockLogger();

        Method method = findMethod(fixture, "build_fail");

        trainForConstructor(resources, logger);

        train_getModuleBuilder(resources, fixture);

        train_isDebugEnabled(logger, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(resources, CREATOR_DESCRIPTION, method);

        try
        {
            sc.createObject();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Error invoking service builder method "
                    + CREATOR_DESCRIPTION + " (for service 'Fie'): Method failed.");

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

        ServiceBuilderResources resources = mockServiceBuilderResources(tracker);
        Logger logger = mockLogger();

        fixture.fie = mockFieService();
        fixture.expectedFoe = mockFoeService();

        trainForConstructor(resources, logger);

        train_getModuleBuilder(resources, fixture);

        expect(resources.getObject(eq(FoeService.class), isA(AnnotationProvider.class))).andReturn(
                fixture.expectedFoe);

        train_isDebugEnabled(logger, false);

        replay();

        ObjectCreator sc = new ServiceBuilderMethodInvoker(resources, CREATOR_DESCRIPTION, method);

        Object actual = sc.createObject();

        verify();

        assertSame(actual, fixture.fie);
    }

    protected final void train_getModuleBuilder(ServiceBuilderResources resources,
                                                Object moduleInstance)
    {
        expect(resources.getModuleBuilder()).andReturn(moduleInstance);
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

    private FoeService mockFoeService()
    {
        return newMock(FoeService.class);
    }

    private FieService mockFieService()
    {
        return newMock(FieService.class);
    }
}
