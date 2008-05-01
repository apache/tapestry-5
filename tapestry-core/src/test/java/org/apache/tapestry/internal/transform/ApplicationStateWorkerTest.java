// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.transform;

import javassist.CtClass;
import javassist.Loader;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.services.*;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.internal.transform.pages.MaybeStateHolder;
import org.apache.tapestry.internal.transform.pages.StateHolder;
import org.apache.tapestry.ioc.internal.services.ClassFactoryClassPool;
import org.apache.tapestry.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry.ioc.internal.services.PropertyAccessImpl;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ApplicationStateManager;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ApplicationStateWorkerTest extends InternalBaseTestCase
{
    private final ClassLoader _contextClassLoader = Thread.currentThread().getContextClassLoader();

    private PropertyAccess _access = new PropertyAccessImpl();

    private ClassFactory _classFactory;

    private Loader _loader;

    private ClassFactoryClassPool _classFactoryClassPool;

    /**
     * We need a new ClassPool for each individual test, since many of the tests will end up modifying one or more
     * CtClass instances.
     */
    @BeforeMethod
    public void setup_classpool()
    {
        //  _classPool = new ClassPool();

        _classFactoryClassPool = new ClassFactoryClassPool(_contextClassLoader);

        _loader = new TestPackageAwareLoader(_contextClassLoader, _classFactoryClassPool);

        // Inside Maven Surefire, the system classpath is not sufficient to find all
        // the necessary files.
        _classFactoryClassPool.appendClassPath(new LoaderClassPath(_loader));

        Logger logger = LoggerFactory.getLogger(InternalClassTransformationImplTest.class);

        _classFactory = new ClassFactoryImpl(_loader, _classFactoryClassPool, logger);
    }

    private CtClass findCtClass(Class targetClass) throws NotFoundException
    {
        return _classFactoryClassPool.get(targetClass.getName());
    }

    private Class toClass(CtClass ctClass) throws Exception
    {
        return _classFactoryClassPool.toClass(ctClass, _loader, null);
    }

    @AfterClass
    public void cleanup()
    {
        _access = null;
    }

    @Test
    public void no_fields_with_annotation()
    {
        ApplicationStateManager manager = mockApplicationStateManager();
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findFieldsWithAnnotation(ct, ApplicationState.class);

        replay();

        ComponentClassTransformWorker worker = new ApplicationStateWorker(manager, null);

        worker.transform(ct, model);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void field_read_and_write() throws Exception
    {
        ApplicationStateManager manager = mockApplicationStateManager();
        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel();
        InternalComponentResources resources = mockInternalComponentResources();
        ComponentClassCache cache = mockComponentClassCache();

        train_getLogger(model, logger);

        Class asoClass = SimpleASO.class;

        CtClass ctClass = findCtClass(StateHolder.class);

        train_forName(cache, asoClass);

        replay();

        InternalClassTransformation transformation = new InternalClassTransformationImpl(_classFactory, ctClass, null,
                                                                                         model, null);
        new ApplicationStateWorker(manager, cache).transform(transformation, model);

        verify();

        transformation.finish();

        Instantiator instantiator = transformation.createInstantiator();

        Object component = instantiator.newInstance(resources);

        // Test the companion flag field

        expect(manager.exists(asoClass)).andReturn(true);

        replay();

        assertEquals(_access.get(component, "beanExists"), true);

        verify();

        // Test read property (get from ASM)

        Object aso = new SimpleASO();

        train_get(manager, asoClass, aso);

        replay();

        assertSame(_access.get(component, "bean"), aso);

        verify();

        // Test write property (set ASM)

        Object aso2 = new SimpleASO();

        manager.set(asoClass, aso2);

        replay();

        _access.set(component, "bean", aso2);

        verify();
    }


    @Test
    public void read_field_with_create_disabled() throws Exception
    {
        ApplicationStateManager manager = mockApplicationStateManager();
        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel();
        InternalComponentResources resources = mockInternalComponentResources();
        ComponentClassCache cache = mockComponentClassCache();

        train_getLogger(model, logger);

        Class asoClass = SimpleASO.class;

        CtClass ctClass = findCtClass(MaybeStateHolder.class);

        train_forName(cache, asoClass);

        replay();

        InternalClassTransformation transformation = new InternalClassTransformationImpl(_classFactory, ctClass, null,
                                                                                         model, null);
        new ApplicationStateWorker(manager, cache).transform(transformation, model);

        verify();

        transformation.finish();

        Instantiator instantiator = transformation.createInstantiator();

        Object component = instantiator.newInstance(resources);

        // Test read property

        train_getIfExists(manager, asoClass, null);

        replay();

        assertNull(_access.get(component, "bean"));

        verify();


        Object aso = new SimpleASO();

        train_getIfExists(manager, asoClass, aso);

        replay();

        assertSame(_access.get(component, "bean"), aso);

        verify();
    }

    protected final void train_getIfExists(ApplicationStateManager manager, Class asoClass, Object aso)
    {
        expect(manager.getIfExists(asoClass)).andReturn(aso);
    }

}
