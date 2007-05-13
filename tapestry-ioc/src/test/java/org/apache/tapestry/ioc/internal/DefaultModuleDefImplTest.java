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
import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.contains;

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.AutobuildModule;
import org.apache.tapestry.ioc.IOCConstants;
import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.StringHolder;
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

        Log log = mockLog();

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
        assertEquals(sd.getServiceScope(), IOCConstants.DEFAULT_SCOPE);
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
        Log log = mockLog();

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
        String conflictMethodString = InternalUtils.asString(conflictMethod, _classFactory);

        String expectedMethod = InternalUtils.asString(ServiceIdConflictMethodModule.class
                .getMethod("buildFred", Object.class), _classFactory);

        Log log = mockLog();

        log.warn(buildMethodConflict(conflictMethodString, expectedMethod), null);

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

        Log log = mockLog();

        log.warn(IOCMessages.buildMethodWrongReturnType(m), null);

        replay();

        ModuleDef md = new DefaultModuleDefImpl(VoidBuilderMethodModule.class, log, null);

        assertTrue(md.getServiceIds().isEmpty());

        verify();
    }

    @Test
    public void builder_method_returns_array() throws Exception
    {
        Method m = BuilderMethodModule.class.getMethod("buildStringArray");

        Log log = mockLog();

        log.warn(IOCMessages.buildMethodWrongReturnType(m), null);

        replay();

        ModuleDef md = new DefaultModuleDefImpl(BuilderMethodModule.class, log, null);

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

        Log log = mockLog();

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

        Log log = mockLog();

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
        Log log = mockLog();

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

        Log log = mockLog();
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

        Log log = mockLog();
        log.warn(IOCMessages.noContributionParameter(m));

        replay();

        ModuleDef md = new DefaultModuleDefImpl(moduleClass, log, null);

        assertTrue(md.getContributionDefs().isEmpty());

        verify();
    }

    @Test
    public void simple_binder_method()
    {
        Log log = mockLog();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(AutobuildModule.class, log, _classFactory);

        ServiceDef sd = md.getServiceDef("StringHolder");

        assertEquals(sd.getServiceInterface(), StringHolder.class);
        assertEquals(sd.getServiceId(), "StringHolder");
        assertEquals(sd.getServiceScope(), IOCConstants.DEFAULT_SCOPE);
        assertFalse(sd.isEagerLoad());

        verify();
    }

    @Test
    public void bind_service_with_all_options()
    {
        Log log = mockLog();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(ComplexAutobuildModule.class, log, _classFactory);

        ServiceDef sd = md.getServiceDef("SH");

        assertEquals(sd.getServiceInterface(), StringHolder.class);
        assertEquals(sd.getServiceId(), "SH");
        assertEquals(sd.getServiceScope(), "magic");
        assertTrue(sd.isEagerLoad());

        verify();
    }

    @Test
    public void attempt_to_bind_a_service_with_no_public_constructor()
    {
        Log log = mockLog();

        replay();

        try
        {
            new DefaultModuleDefImpl(UninstantiableAutobuildServiceModule.class, log, _classFactory);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Class org.apache.tapestry.ioc.internal.RunnableServiceImpl (implementation of service \'Runnable\') does not contain any public constructors.");
        }

        verify();
    }

    @Test
    public void instance_method_bind_is_ignored()
    {
        Log log = mockLog();

        log.error(and(
                contains(NonStaticBindMethodModule.class.getName()),
                contains("but is an instance method")));

        replay();

        ModuleDef md = new DefaultModuleDefImpl(NonStaticBindMethodModule.class, log, _classFactory);

        // Prove that the bind method was not invoke

        assertTrue(md.getServiceIds().isEmpty());

        verify();
    }

    @Test
    public void multiple_constructors_on_autobuild_service_implementation()
    {
        Log log = mockLog();
        ServiceBuilderResources resources = mockServiceBuilderResources();

        train_isDebugEnabled(log, true);

        // The point is, we're choosing the constructor with the largest number of parameters.

        log
                .debug(contains("Invoking constructor org.apache.tapestry.ioc.internal.MultipleConstructorsAutobuildService(StringHolder)"));

        train_getServiceId(resources, "StringHolder");
        train_getServiceLog(resources, log);
        train_getServiceInterface(resources, StringHolder.class);
        train_getService(
                resources,
                "ToUpperCaseStringHolder",
                StringHolder.class,
                new ToUpperCaseStringHolder());

        replay();

        ModuleDef def = new DefaultModuleDefImpl(MutlipleAutobuildServiceConstructorsModule.class,
                log, _classFactory);

        ServiceDef sd = def.getServiceDef("StringHolder");

        assertNotNull(sd);

        ObjectCreator oc = sd.createServiceCreator(resources);

        StringHolder holder = (StringHolder) oc.createObject();

        holder.setValue("foo");
        assertEquals(holder.getValue(), "FOO");

        verify();
    }

    @Test
    public void exception_from_inside_bind_method()
    {
        Log log = mockLog();

        replay();

        try
        {
            new DefaultModuleDefImpl(ExceptionInBindMethod.class, log, _classFactory);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex
                    .getMessage()
                    .matches(
                            "Error invoking service binder method org.apache.tapestry.ioc.internal.ExceptionInBindMethod.bind\\(ServiceBinder\\) "
                                    + "\\(at ExceptionInBindMethod.java:\\d+\\): Really, how often is this going to happen\\?"));
        }

        verify();
    }

    @Test
    public void autoload_service_is_eager_load_via_annotation()
    {
        Log log = mockLog();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(EagerLoadViaAnnotationModule.class, log,
                _classFactory);

        ServiceDef sd = md.getServiceDef("Runnable");

        assertTrue(sd.isEagerLoad());

        verify();
    }
}
