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
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.IOCConstants;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.def.DecoratorDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DefaultModuleDefImplTest extends IOCTestCase
{
    private ClassFactory _classFactory;

    @BeforeClass
    public void setup()
    {
        _classFactory = new ClassFactoryImpl();
    }

    @AfterClass
    public void cleanup()
    {
        _classFactory = null;
    }

    @Test
    public void simple_module() throws Exception
    {
        String className = SimpleModule.class.getName();

        Log log = newLog();

        replay();

        // BigDecimal is arbitrary, any class would do.

        ModuleDef md = new DefaultModuleDefImpl(SimpleModule.class, log, _classFactory);

        assertEquals(md.toString(), "ModuleDef[" + className + " Barney, Fred, Wilma]");

        Set<String> ids = md.getServiceIds();

        assertEquals(ids.size(), 3);
        assertTrue(ids.contains("Fred"));
        assertTrue(ids.contains("Barney"));
        assertTrue(ids.contains("Wilma"));

        ServiceDef sd = md.getServiceDef("Fred");

        assertEquals(sd.getServiceId(), "Fred");

        assertEquals(sd.getServiceInterface(), FieService.class);

        assertTrue(sd.toString().contains(className + ".buildFred()"));
        assertEquals(sd.getServiceLifeycle(), IOCConstants.DEFAULT_LIFECYCLE);
        assertEquals(sd.isEagerLoad(), false);

        sd = md.getServiceDef("Wilma");
        assertEquals(sd.isEagerLoad(), true);

        // Now the decorator method.

        Set<DecoratorDef> defs = md.getDecoratorDefs();

        assertEquals(defs.size(), 1);

        DecoratorDef dd = defs.iterator().next();

        assertEquals(dd.getDecoratorId(), "Logging");
        assertTrue(dd.toString().contains(className + ".decorateLogging(Class, Object)"));

        verify();
    }

    @Test
    public void default_service_id_from_return_type()
    {
        Log log = newLog();

        replay();

        ModuleDef def = new DefaultModuleDefImpl(DefaultServiceIdModule.class, log, null);

        assertEquals(def.getServiceIds().size(), 1);

        ServiceDef sd = def.getServiceDef("FieService");

        assertEquals(sd.getServiceId(), "FieService");

        verify();
    }

    /** Two different methods both claim to build the same service. */
    @Test
    public void service_id_conflict() throws Exception
    {
        Method conflictMethod = ServiceIdConflictMethodModule.class.getMethod("buildFred");
        String expectedMethod = InternalUtils.asString(ServiceIdConflictMethodModule.class
                .getMethod("buildFred", Object.class), _classFactory);

        Log log = newLog();

        log.warn(buildMethodConflict(conflictMethod, expectedMethod), null);

        replay();

        // BigDecimal is arbitrary, any class would do.

        ModuleDef md = new DefaultModuleDefImpl(ServiceIdConflictMethodModule.class, log,
                _classFactory);

        Set<String> ids = md.getServiceIds();

        assertEquals(ids.size(), 1);
        assertTrue(ids.contains("Fred"));

        ServiceDef sd = md.getServiceDef("Fred");

        assertEquals(sd.getServiceId(), "Fred");

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

        ModuleDef md = new DefaultModuleDefImpl(VoidBuilderMethodModule.class, log, null);

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

        ModuleDef md = new DefaultModuleDefImpl(moduleClass, log, null);

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

        ModuleDef md = new DefaultModuleDefImpl(moduleClass, log, null);

        assertTrue(md.getDecoratorDefs().isEmpty());

        verify();
    }

    @Test
    public void contribution_without_annotation()
    {
        attemptConfigurationMethod(SimpleModule.class, "Barney", "contributeBarney(Configuration)");
    }

    @Test
    public void ordered_contribution_method()
    {
        attemptConfigurationMethod(
                OrderedConfigurationModule.class,
                "Ordered",
                "contributeOrdered(OrderedConfiguration)");
    }

    @Test
    public void mapped_contribution_method()
    {
        attemptConfigurationMethod(
                MappedConfigurationModule.class,
                "Mapped",
                "contributeMapped(MappedConfiguration)");
    }

    private void attemptConfigurationMethod(Class moduleClass, String expectedServiceId,
            String expectedMethodSignature)
    {
        Log log = newLog();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(moduleClass, log, _classFactory);

        Set<ContributionDef> defs = md.getContributionDefs();

        assertEquals(defs.size(), 1);

        ContributionDef cd = defs.iterator().next();

        // The target service id is derived from the method name

        assertEquals(cd.getServiceId(), expectedServiceId);

        // Can't be exact, because the source file & line number are probably attached (and those
        // can change)

        assertTrue(cd.toString().contains(moduleClass.getName() + "." + expectedMethodSignature));

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

        ModuleDef md = new DefaultModuleDefImpl(moduleClass, log, null);

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

        ModuleDef md = new DefaultModuleDefImpl(moduleClass, log, null);

        assertTrue(md.getContributionDefs().isEmpty());

        verify();
    }
}
