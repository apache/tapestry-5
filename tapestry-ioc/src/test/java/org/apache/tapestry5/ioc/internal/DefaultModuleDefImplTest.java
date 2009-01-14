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

import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import static org.easymock.EasyMock.contains;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Set;

public class DefaultModuleDefImplTest extends IOCTestCase
{
    private ClassFactory classFactory;

    private final OperationTracker tracker = new QuietOperationTracker();

    @BeforeClass
    public void setup()
    {
        classFactory = new ClassFactoryImpl();
    }

    @AfterClass
    public void cleanup()
    {
        classFactory = null;
    }

    @Test
    public void simple_module() throws Exception
    {
        String className = SimpleModule.class.getName();

        Logger logger = mockLogger();

        replay();

        // BigDecimal is arbitrary, any class would do.

        ModuleDef md = new DefaultModuleDefImpl(SimpleModule.class, logger, classFactory);

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
        assertEquals(sd.getServiceScope(), ScopeConstants.DEFAULT);
        assertEquals(sd.isEagerLoad(), false);
        assertTrue(sd.getMarkers().isEmpty());

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
    public void default_service_id_from_method_annotation()
    {
        Logger logger = mockLogger();

        replay();

        ModuleDef def = new DefaultModuleDefImpl(ServiceIdViaAnnotationModule.class, logger, null);

        assertEquals(def.getServiceIds().size(), 2);

        ServiceDef sd = def.getServiceDef("FooService");

        assertEquals(sd.getServiceId(), "FooService");

        verify();
    }
    
    @Test
    public void default_service_id_from_annotation()
    {
        Logger logger = mockLogger();

        replay();

        ModuleDef def = new DefaultModuleDefImpl(ServiceIdViaAnnotationModule.class, logger, null);

        assertEquals(def.getServiceIds().size(), 2);

        ServiceDef sd = def.getServiceDef("BarneyService");

        assertEquals(sd.getServiceId(), "BarneyService");

        verify();
    }
    
    @Test
    public void default_service_id_from_return_type()
    {
        Logger logger = mockLogger();

        replay();

        ModuleDef def = new DefaultModuleDefImpl(DefaultServiceIdModule.class, logger, null);

        assertEquals(def.getServiceIds().size(), 1);

        ServiceDef sd = def.getServiceDef("FieService");

        assertEquals(sd.getServiceId(), "FieService");

        verify();
    }

    /**
     * Two different methods both claim to build the same service.
     */
    @Test
    public void service_id_conflict() throws Exception
    {
        Method conflictMethod = ServiceIdConflictMethodModule.class.getMethod("buildFred");
        String conflictMethodString = InternalUtils.asString(conflictMethod, classFactory);

        String expectedMethod = InternalUtils.asString(ServiceIdConflictMethodModule.class
                .getMethod("buildFred", Object.class), classFactory);

        Logger logger = mockLogger();

        replay();

        // BigDecimal is arbitrary, any class would do.

        try
        {
            new DefaultModuleDefImpl(ServiceIdConflictMethodModule.class, logger, classFactory);

            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "Service Fred (defined by org.apache.tapestry5.ioc.internal.ServiceIdConflictMethodModule.buildFred()",
                                  "conflicts with previously defined service defined by org.apache.tapestry5.ioc.internal.ServiceIdConflictMethodModule.buildFred(Object)");
        }


        verify();
    }

    @Test
    public void builder_method_returns_void() throws Exception
    {
        Method m = VoidBuilderMethodModule.class.getMethod("buildNull");

        Logger logger = mockLogger();

        replay();

        try
        {
            new DefaultModuleDefImpl(VoidBuilderMethodModule.class, logger, null);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), IOCMessages.buildMethodWrongReturnType(m));
        }

        verify();
    }

    @Test
    public void builder_method_returns_array() throws Exception
    {
        Method m = BuilderMethodModule.class.getMethod("buildStringArray");

        Logger logger = mockLogger();

        replay();

        try
        {
            new DefaultModuleDefImpl(BuilderMethodModule.class, logger, null);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), IOCMessages.buildMethodWrongReturnType(m));
        }

        verify();
    }

    @Test
    public void decorator_method_returns_void() throws Exception
    {
        invalidDecoratorMethod(VoidDecoratorMethodModule.class, "decorateVoid");
    }

    private void invalidDecoratorMethod(Class moduleClass, String methodName) throws NoSuchMethodException
    {
        Method m = moduleClass.getMethod(methodName, Object.class);

        Logger logger = mockLogger();

        replay();

        try
        {
            new DefaultModuleDefImpl(moduleClass, logger, null);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), IOCMessages.decoratorMethodWrongReturnType(m));
        }

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
    public void contribution_without_annotation()
    {
        attemptConfigurationMethod(SimpleModule.class, "Barney", "contributeBarney(Configuration)");
    }

    @Test
    public void ordered_contribution_method()
    {
        attemptConfigurationMethod(OrderedConfigurationModule.class, "Ordered",
                                   "contributeOrdered(OrderedConfiguration)");
    }

    @Test
    public void mapped_contribution_method()
    {
        attemptConfigurationMethod(MappedConfigurationModule.class, "Mapped", "contributeMapped(MappedConfiguration)");
    }

    private void attemptConfigurationMethod(Class moduleClass, String expectedServiceId, String expectedMethodSignature)
    {
        Logger logger = mockLogger();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(moduleClass, logger, classFactory);

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

        Logger logger = mockLogger();

        replay();

        try
        {
            new DefaultModuleDefImpl(moduleClass, logger, null);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                         "Service contribution method org.apache.tapestry5.ioc.internal.TooManyContributionParametersModule.contributeTooMany(Configuration, OrderedConfiguration) contains more than one parameter of type Configuration, OrderedConfiguration, or MappedConfiguration. Exactly one such parameter is required for a service contribution method.");
        }


        verify();
    }

    @Test
    public void contribution_with_no_contribution_parameter() throws Exception
    {
        Class moduleClass = NoUsableContributionParameterModule.class;
        Method m = findMethod(moduleClass, "contributeNoParameter");

        Logger logger = mockLogger();

        replay();

        try
        {
            new DefaultModuleDefImpl(moduleClass, logger, null);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                         "Service contribution method org.apache.tapestry5.ioc.internal.NoUsableContributionParameterModule.contributeNoParameter(UpcaseService) does not contain a parameter of type Configuration, OrderedConfiguration or MappedConfiguration. This parameter is how the method make contributions into the service's configuration.");
        }

        verify();
    }

    @Test
    public void simple_binder_method()
    {
        Logger logger = mockLogger();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(AutobuildModule.class, logger, classFactory);

        ServiceDef sd = md.getServiceDef("StringHolder");

        assertEquals(sd.getServiceInterface(), StringHolder.class);
        assertEquals(sd.getServiceId(), "StringHolder");
        assertEquals(sd.getServiceScope(), ScopeConstants.DEFAULT);
        assertFalse(sd.isEagerLoad());

        verify();
    }

    @Test
    public void bind_service_with_all_options()
    {
        Logger logger = mockLogger();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(ComplexAutobuildModule.class, logger, classFactory);

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
        Logger logger = mockLogger();

        replay();

        try
        {
            new DefaultModuleDefImpl(UninstantiableAutobuildServiceModule.class, logger, classFactory);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "Class org.apache.tapestry5.ioc.internal.RunnableServiceImpl (implementation of service \'Runnable\') does not contain any public constructors.");
        }

        verify();
    }

    @Test
    public void instance_method_bind_is_error()
    {
        Logger logger = mockLogger();

        replay();

        try
        {
            new DefaultModuleDefImpl(NonStaticBindMethodModule.class, logger, classFactory);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "Method org.apache.tapestry5.ioc.internal.NonStaticBindMethodModule.bind(ServiceBinder)",
                                  "appears to be a service binder method, but is an instance method, not a static method.");
        }

        verify();
    }

    @Test
    public void multiple_constructors_on_autobuild_service_implementation()
    {
        Logger logger = mockLogger();
        ServiceBuilderResources resources = mockServiceBuilderResources();

        train_getTracker(resources, tracker);

        train_isDebugEnabled(logger, true);

        // The point is, we're choosing the constructor with the largest number of parameters.

        logger
                .debug(contains(
                        "Invoking constructor org.apache.tapestry5.ioc.internal.MultipleConstructorsAutobuildService(StringHolder)"));

        train_getServiceId(resources, "StringHolder");
        train_getLogger(resources, logger);
        train_getServiceInterface(resources, StringHolder.class);
        train_getService(resources, "ToUpperCaseStringHolder", StringHolder.class, new ToUpperCaseStringHolder());

        replay();

        ModuleDef def = new DefaultModuleDefImpl(MutlipleAutobuildServiceConstructorsModule.class, logger,
                                                 classFactory);

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
        Logger logger = mockLogger();

        replay();

        try
        {
            new DefaultModuleDefImpl(ExceptionInBindMethod.class, logger, classFactory);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex
                    .getMessage()
                    .matches(
                    "Error invoking service binder method org.apache.tapestry5.ioc.internal.ExceptionInBindMethod.bind\\(ServiceBinder\\) " + "\\(at ExceptionInBindMethod.java:\\d+\\): Really, how often is this going to happen\\?"));
        }

        verify();
    }

    @Test
    public void autoload_service_is_eager_load_via_annotation()
    {
        Logger logger = mockLogger();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(EagerLoadViaAnnotationModule.class, logger, classFactory);

        ServiceDef sd = md.getServiceDef("Runnable");

        assertTrue(sd.isEagerLoad());

        verify();
    }

    @Test
    public void service_builder_method_has_marker_annotation()
    {
        Logger logger = mockLogger();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(MarkerModule.class, logger, classFactory);

        ServiceDef sd = md.getServiceDef("Greeter");

        assertListsEquals(CollectionFactory.newList(sd.getMarkers()), BlueMarker.class);

        verify();
    }

    @Test
    public void bound_service_has_marker_annotation()
    {
        Logger logger = mockLogger();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(MarkerModule.class, logger, classFactory);

        ServiceDef sd = md.getServiceDef("RedGreeter");

        assertListsEquals(CollectionFactory.newList(sd.getMarkers()), RedMarker.class);

        verify();
    }

    @Test
    public void bound_service_explicit_marker()
    {
        Logger logger = mockLogger();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(MarkerModule.class, logger, classFactory);

        ServiceDef sd = md.getServiceDef("SecondRedGreeter");

        assertListsEquals(CollectionFactory.newList(sd.getMarkers()), RedMarker.class);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void explicit_marker_overrides_marker_annotation()
    {
        Logger logger = mockLogger();

        replay();

        ModuleDef md = new DefaultModuleDefImpl(MarkerModule.class, logger, classFactory);

        ServiceDef sd = md.getServiceDef("SurprisinglyBlueGreeter");

        // BlueMarker from ServiceBindingOptions, RedMarker from @Marker on class

        Set<Class> markers = sd.getMarkers();

        assertTrue(markers.contains(RedMarker.class));
        assertTrue(markers.contains(BlueMarker.class));
        assertEquals(markers.size(), 2);

        verify();
    }

    // TODO: We're short on tests that ensure that marker annotation are additive (i.e., module
    // marker annotation are
    // merged into the set specific to the service).
}
