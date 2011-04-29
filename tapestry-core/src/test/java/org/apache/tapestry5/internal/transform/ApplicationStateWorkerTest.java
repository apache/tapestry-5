// Copyright 2007, 2008, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import javassist.CtClass;
import javassist.Loader;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryClassPool;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry5.ioc.internal.services.PropertyAccessImpl;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;

/**
 * This test was gutted because it depended too much on internals (i.e., instantiating an
 * InternalComponentClassTransformation instance). Still, unit tests (rather than relying entirely
 * on integration tests) would be nice.
 */
public class ApplicationStateWorkerTest extends InternalBaseTestCase
{
    private final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

    private PropertyAccess access = new PropertyAccessImpl();

    private ClassFactory classFactory;

    private Loader loader;

    private ClassFactoryClassPool classFactoryClassPool;

    /**
     * We need a new ClassPool for each individual test, since many of the tests will end up modifying one or more
     * CtClass instances.
     */
    @BeforeMethod
    public void setup_classpool()
    {
        // _classPool = new ClassPool();

        classFactoryClassPool = new ClassFactoryClassPool(contextClassLoader);

        loader = new TestPackageAwareLoader(contextClassLoader, classFactoryClassPool);

        // Inside Maven Surefire, the system classpath is not sufficient to find all
        // the necessary files.
        classFactoryClassPool.appendClassPath(new LoaderClassPath(loader));

        Logger logger = LoggerFactory.getLogger(ApplicationStateWorkerTest.class);

        classFactory = new ClassFactoryImpl(loader, classFactoryClassPool, logger);
    }

    private CtClass findCtClass(Class targetClass) throws NotFoundException
    {
        return classFactoryClassPool.get(targetClass.getName());
    }

    private Class toClass(CtClass ctClass) throws Exception
    {
        return classFactoryClassPool.toClass(ctClass, loader, null);
    }

    @AfterClass
    public void cleanup()
    {
        access = null;
    }

    protected final void train_getIfExists(ApplicationStateManager manager, Class asoClass, Object aso)
    {
        expect(manager.getIfExists(asoClass)).andReturn(aso);
    }

}
