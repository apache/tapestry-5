// Copyright 2007 The Apache Software Foundation
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

import javassist.ClassPool;
import javassist.CtClass;
import javassist.Loader;
import javassist.LoaderClassPath;

import org.apache.commons.logging.Log;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.internal.services.PropertyAccessImpl;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ApplicationStateManager;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class ApplicationStateWorkerTest extends InternalBaseTestCase
{
    private final ClassLoader _contextClassLoader = Thread.currentThread().getContextClassLoader();

    private PropertyAccess _access = new PropertyAccessImpl();

    @AfterClass
    public void cleanup()
    {
        _access = null;
    }

    @Test
    public void no_fields_with_annotation()
    {
        ApplicationStateManager manager = newApplicationStateManager();
        ClassTransformation ct = mockClassTransformation();
        MutableComponentModel model = mockMutableComponentModel();

        train_findFieldsWithAnnotation(ct, ApplicationState.class);

        replay();

        ComponentClassTransformWorker worker = new ApplicationStateWorker(manager);

        worker.transform(ct, model);

        verify();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void field_read_and_write() throws Exception
    {
        ApplicationStateManager manager = newApplicationStateManager();
        Log log = mockLog();
        MutableComponentModel model = mockMutableComponentModel();
        InternalComponentResources resources = mockInternalComponentResources();

        String componentClassName = StateHolder.class.getName();
        Class asoClass = ReadOnlyBean.class;

        ClassPool pool = new ClassPool();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        pool.appendClassPath(new LoaderClassPath(contextLoader));

        Loader loader = new Loader(contextLoader, pool);

        loader.delegateLoadingOf("org.apache.tapestry.");

        CtClass ctClass = pool.get(componentClassName);
        InternalClassTransformation transformation = new InternalClassTransformationImpl(ctClass,
                _contextClassLoader, log, null);

        replay();

        new ApplicationStateWorker(manager).transform(transformation, model);

        verify();

        transformation.finish();

        Class transformedClass = pool.toClass(ctClass, loader);

        Instantiator instantiator = transformation.createInstantiator(transformedClass);

        Object component = instantiator.newInstance(resources);

        // Test the companion flag field

        expect(manager.exists(asoClass)).andReturn(true);

        replay();

        assertEquals(_access.get(component, "beanExists"), true);

        verify();

        // Test read property (get from ASM)

        Object aso = new ReadOnlyBean();

        train_get(manager, asoClass, aso);

        replay();

        assertSame(_access.get(component, "bean"), aso);

        verify();

        // Test write property (set ASM)

        Object aso2 = new ReadOnlyBean();

        manager.set(asoClass, aso2);

        replay();

        _access.set(component, "bean", aso2);

        verify();
    }

    protected final ApplicationStateManager newApplicationStateManager()
    {
        return newMock(ApplicationStateManager.class);
    }

    protected final <T> void train_get(ApplicationStateManager manager, Class<T> asoClass, T aso)
    {
        expect(manager.get(asoClass)).andReturn(aso);
    }
}
