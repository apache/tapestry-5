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

package org.apache.tapestry.ioc.internal;

import static org.apache.tapestry.ioc.internal.IOCMessages.buildMethodConflict;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.IOCConstants;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.def.DecoratorDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DefaultModuleDefImplTest extends IOCTestCase
{
    @Test
    public void module_builder_without_id()
    {
        Log log = newLog();

        replay();

        // BigDecimal is arbitrary, any class would do.

        ModuleDef md = new DefaultModuleDefImpl(BigDecimal.class, log);

        Assert.assertEquals("java.math", md.getModuleId());

        verify();
    }

    @Test
    public void module_builder_with_id()
    {
        Log log = newLog();

        replay();

        // BigDecimal is arbitrary, any class would do.

        ModuleDef md = new DefaultModuleDefImpl(ModuleBuilderWithId.class, log);

        Assert.assertEquals("tapestry.ioc", md.getModuleId());

        verify();
    }

    @Test
    public void simple_module() throws Exception
    {
        String className = SimpleModule.class.getName();

        Log log = newLog();

        replay();

        // BigDecimal is arbitrary, any class would do.

        ModuleDef md = new DefaultModuleDefImpl(SimpleModule.class, log);

        Set<String> ids = md.getServiceIds();

        assertEquals(ids.size(), 3);
        assertTrue(ids.contains("ioc.Fred"));
        assertTrue(ids.contains("ioc.Barney"));
        assertTrue(ids.contains("ioc.Wilma"));

        ServiceDef sd = md.getServiceDef("ioc.Fred");

        assertEquals(sd.getServiceId(), "ioc.Fred");

        assertEquals(sd.getServiceInterface(), FieService.class);

        assertEquals(sd.toString(), className + ".buildFred()");
        assertEquals(sd.getServiceLifeycle(), IOCConstants.DEFAULT_LIFECYCLE);
        assertEquals(sd.isPrivate(), false);
        assertEquals(sd.isEagerLoad(), false);

        sd = md.getServiceDef("ioc.Barney");

        assertEquals(sd.getServiceId(), "ioc.Barney");

        assertEquals(sd.getServiceInterface(), FoeService.class);

        assertEquals(sd.toString(), className + ".buildBarney()");
        assertEquals(sd.getServiceLifeycle(), "threaded");
        assertEquals(sd.isPrivate(), true);

        sd = md.getServiceDef("ioc.Wilma");
        assertEquals(sd.isEagerLoad(), true);

        // Now the decorator method.

        Set<DecoratorDef> defs = md.getDecoratorDefs();

        assertEquals(defs.size(), 1);

        DecoratorDef dd = defs.iterator().next();

        assertEquals(dd.getDecoratorId(), "ioc.Logging");
        assertEquals(dd.toString(), className + ".decorateLogging(Class, Object)");

        verify();

    }

    /** Two different methods both claim to build the same service. */
    @Test
    public void service_id_conflict() throws Exception
    {
        Method conflictMethod = ServiceIdConflictMethodModule.class.getMethod("buildFred");
        String expectedMethod = InternalUtils.asString(ServiceIdConflictMethodModule.class
                .getMethod("buildFred", Object.class));

        Log log = newLog();

        log.warn(buildMethodConflict(conflictMethod, expectedMethod), null);

        replay();

        // BigDecimal is arbitrary, any class would do.

        ModuleDef md = new DefaultModuleDefImpl(ServiceIdConflictMethodModule.class, log);

        Set<String> ids = md.getServiceIds();

        assertEquals(ids.size(), 1);
        assertTrue(ids.contains("ioc.Fred"));

        ServiceDef sd = md.getServiceDef("ioc.Fred");

        assertEquals(sd.getServiceId(), "ioc.Fred");

        assertEquals(sd.getServiceInterface(), FieService.class);

        // The methods are considered in ascending order, by name, then descending order, by
        // parameter count. So the grinder will latch onto the method that takes a parameter,
        // and consider the other method (with no parameters) the conflict.

        assertEquals(sd.toString(), expectedMethod.toString());

        verify();
    }

    @Test
    public void builder_method_returns_void() throws Exception
    {
        Method m = VoidBuilderMethodModule.class.getMethod("buildNull");

        Log log = newLog();

        log.warn(IOCMessages.buildMethodWrongReturnType(m), null);

        replay();

        ModuleDef md = new DefaultModuleDefImpl(VoidBuilderMethodModule.class, log);

        assertTrue(md.getServiceIds().isEmpty());

        verify();
    }

    @Test
    public void decorator_method_returns_void() throws Exception
    {
        invalidDecoratorMethod(VoidDecoratorMethodModule.class, "decorateVoid");
    }

    private void invalidDecoratorMethod(Class moduleClass, String methodName)
            throws NoSuchMethodException
    {
        Method m = moduleClass.getMethod(methodName, Object.class);

        Log log = newLog();

        log.warn(IOCMessages.decoratorMethodWrongReturnType(m), null);

        replay();

        ModuleDef md = new DefaultModuleDefImpl(moduleClass, log);

        assertTrue(md.getDecoratorDefs().isEmpty());

        verify();
    }

    @Test
    public void decorator_method_returns_primitive() throws Exception
    {
        invalidDecoratorMethod(PrimitiveDecoratorMethodModule.class, "decoratePrimitive");
    }

    @Test
    public void decorator_method_returns_array() throws Exception
    {
        invalidDecoratorMethod(ArrayDecoratorMethodModule.class, "decorateArray");
    }

    @Test
    public void decorator_method_does_not_include_delegate_parameter() throws Exception
    {
        Class moduleClass = NoDelegateDecoratorMethodModule.class;
        Method m = moduleClass.getMethod("decorateNoDelegate");

        Log log = newLog();

        log.warn(IOCMessages.decoratorMethodNeedsDelegateParameter(m), null);

        replay();

        ModuleDef md = new DefaultModuleDefImpl(moduleClass, log);

        assertTrue(md.getDecoratorDefs().isEmpty());

        verify();
    }

    @Test
    public void contribution_without_annotation()
    {
        attemptConfigurationMethod(
                SimpleModule.class,
                "ioc.Barney",
                "contributeBarney(Configuration)");
    }

    @Test
    public void contribution_with_annotation()
    {
        attemptConfigurationMethod(
                ConfigurationWithContributeAnnotationModule.class,
                "ioc.test.Fred",
                "contributeSomething(Configuration)");

    }

    @Test
    public void contribution_with_annotation_to_other_module()
    {
        attemptConfigurationMethod(
                ConfigurationWithAnnotationOtherModule.class,
                "some.module.Wilma",
                "contributeOtherModule(Configuration)");
    }

    @Test
    public void ordered_contribution_method()
    {
        attemptConfigurationMethod(
                OrderedConfigurationModule.class,
                "ioc.test.Ordered",
                "contributeOrdered(OrderedConfiguration)");
    }

    @Test
    public void mapped_contribution_method()
    {
        attemptConfigurationMethod(
                MappedConfigurationModule.class,
                "ioc.test.Mapped",
                "contributeMapped(MappedConfiguration)");
    }

    private void attemptConfigurationMethod(Class moduleClass, String expectedServiceId,
            String expectedMethodSignature)
    {
        Log log = newLog();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(moduleClass, log);

        Set<ContributionDef> defs = md.getContributionDefs();

        assertEquals(defs.size(), 1);

        ContributionDef cd = defs.iterator().next();

        // The target service id is derived from the method name

        assertEquals(cd.getServiceId(), expectedServiceId);
        assertEquals(cd.toString(), moduleClass.getName() + "." + expectedMethodSignature);

        verify();
    }

    @Test
    public void contribution_with_too_many_parameters() throws Exception
    {
        Class moduleClass = TooManyContributionParametersModule.class;
        Method m = findMethod(moduleClass, "contributeTooMany");

        Log log = newLog();
        log.warn(IOCMessages.tooManyContributionParameters(m));

        replay();

        ModuleDef md = new DefaultModuleDefImpl(moduleClass, log);

        assertTrue(md.getContributionDefs().isEmpty());

        verify();
    }

    @Test
    public void contribution_with_no_contribution_parameter() throws Exception
    {
        Class moduleClass = NoUsableContributionParameterModule.class;
        Method m = findMethod(moduleClass, "contributeNoParameter");

        Log log = newLog();
        log.warn(IOCMessages.noContributionParameter(m));

        replay();

        ModuleDef md = new DefaultModuleDefImpl(moduleClass, log);

        assertTrue(md.getContributionDefs().isEmpty());

        verify();
    }
}
