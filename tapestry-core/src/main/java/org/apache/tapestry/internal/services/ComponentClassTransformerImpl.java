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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newConcurrentMap;

import java.lang.reflect.Modifier;
import java.util.Map;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

import org.apache.tapestry.internal.events.InvalidationListener;
import org.apache.tapestry.internal.model.MutableComponentModelImpl;
import org.apache.tapestry.ioc.LogSource;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.internal.util.ClasspathResource;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.slf4j.Logger;

/**
 * Implementation of {@link org.apache.tapestry.internal.services.ComponentClassTransformer}.
 */
public class ComponentClassTransformerImpl implements ComponentClassTransformer,
        InvalidationListener
{
    /** Map from class name to class transformation. */
    private final Map<String, InternalClassTransformation> _nameToClassTransformation = newConcurrentMap();

    private final Map<String, ComponentModel> _nameToComponentModel = newConcurrentMap();

    private final ComponentClassTransformWorker _workerChain;

    private final LogSource _logSource;

    /**
     * @param workerChain
     *            the ordered list of class transform works as a chain of command instance
     */
    public ComponentClassTransformerImpl(ComponentClassTransformWorker workerChain,
            LogSource logSource)
    {
        _workerChain = workerChain;
        _logSource = logSource;
    }

    /**
     * Clears the cache of {@link InternalClassTransformation} instances whenever the class loader
     * is invalidated.
     */
    public void objectWasInvalidated()
    {
        _nameToClassTransformation.clear();
        _nameToComponentModel.clear();
    }

    public void transformComponentClass(CtClass ctClass, ClassLoader classLoader)
    {
        String parentClassname;

        // Component classes must be public

        if (!Modifier.isPublic(ctClass.getModifiers())) return;

        try
        {
            // And have a public constructor.

            CtConstructor ctor = ctClass.getConstructor("()V");

            if (!Modifier.isPublic(ctor.getModifiers())) return;
        }
        catch (NotFoundException ex)
        {
            return;
        }

        // Force the creation of the parent class.

        try
        {
            parentClassname = ctClass.getSuperclass().getName();
        }
        catch (NotFoundException ex)
        {
            throw new RuntimeException(ex);
        }

        String classname = ctClass.getName();

        Logger logger = _logSource.getLogger(classname);

        // If the parent class is in a controlled package, it will already have been loaded and
        // transformed (that is driven by the ComponentInstantiatorSource).

        InternalClassTransformation parentTransformation = _nameToClassTransformation
                .get(parentClassname);

        // TODO: Check that the name is not already in the map. But I think that can't happen,
        // because the classloader itself is synchronized.

        Resource baseResource = new ClasspathResource(classname.replace(".", "/") + ".class");

        ComponentModel parentModel = _nameToComponentModel.get(parentClassname);

        MutableComponentModel model = new MutableComponentModelImpl(classname, logger,
                baseResource, parentModel);

        InternalClassTransformation transformation = parentTransformation == null ? new InternalClassTransformationImpl(
                ctClass, classLoader, logger, model)
                : new InternalClassTransformationImpl(ctClass, parentTransformation, classLoader,
                        logger, model);

        try
        {
            _workerChain.transform(transformation, model);

            transformation.finish();
        }
        catch (Throwable ex)
        {
            throw new TransformationException(transformation, ex);
        }

        if (logger.isDebugEnabled())
            logger.debug("Finished class transformation: " + transformation);

        _nameToClassTransformation.put(classname, transformation);
        _nameToComponentModel.put(classname, model);
    }

    public Instantiator createInstantiator(Class componentClass)
    {
        String className = componentClass.getName();

        InternalClassTransformation ct = _nameToClassTransformation.get(className);

        if (ct == null)
            throw new RuntimeException(ServicesMessages.classNotTransformed(className));

        try
        {
            return ct.createInstantiator(componentClass);
        }
        catch (Throwable ex)
        {
            throw new TransformationException(ct, ex);
        }
    }
}
