// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import org.apache.tapestry5.TapestryMarkers;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.model.MutableComponentModelImpl;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.services.CtClassSource;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentLayer;
import org.apache.tapestry5.services.InvalidationListener;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Implementation of {@link org.apache.tapestry5.internal.services.ComponentClassTransformer}.
 */
public class ComponentClassTransformerImpl implements ComponentClassTransformer, InvalidationListener
{
    /**
     * Map from class name to class transformation.
     */
    private final Map<String, InternalClassTransformation> nameToClassTransformation = CollectionFactory.newConcurrentMap();

    private final Map<String, ComponentModel> nameToComponentModel = CollectionFactory.newConcurrentMap();

    private final ComponentClassTransformWorker workerChain;

    private final LoggerSource loggerSource;

    private final ClassFactory classFactory;

    private final CtClassSource classSource;

    private final ComponentClassCache componentClassCache;

    private final String[] SUBPACKAGES = {"." + InternalConstants.PAGES_SUBPACKAGE + ".",
            "." + InternalConstants.COMPONENTS_SUBPACKAGE + ".",
            "." + InternalConstants.MIXINS_SUBPACKAGE + ".",
            "." + InternalConstants.BASE_SUBPACKAGE + "."};

    /**
     * @param workerChain         the ordered list of class transform works as a chain of command instance
     * @param classSource
     * @param componentClassCache
     */
    public ComponentClassTransformerImpl(ComponentClassTransformWorker workerChain,
                                         LoggerSource loggerSource,
                                         @ComponentLayer ClassFactory classFactory,
                                         @ComponentLayer CtClassSource classSource,
                                         ComponentClassCache componentClassCache)
    {
        this.workerChain = workerChain;
        this.loggerSource = loggerSource;
        this.classFactory = classFactory;
        this.componentClassCache = componentClassCache;
        this.classSource = classSource;
    }

    /**
     * Clears the cache of {@link InternalClassTransformation} instances whenever the class loader is invalidated.
     */
    public void objectWasInvalidated()
    {
        nameToClassTransformation.clear();
        nameToComponentModel.clear();
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

        // Is it an inner class (does the class name contain a '$')?
        // Inner classes are loaded by the same class loader as the component, but are
        // not components and are not transformed.


        if (ctClass.getName().contains("$")) return;

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

        Logger transformLogger = loggerSource.getLogger("tapestry.transformer." + classname);
        Logger logger = loggerSource.getLogger(classname);

        // If the parent class is in a controlled package, it will already have been loaded and
        // transformed (that is driven by the ComponentInstantiatorSource).

        InternalClassTransformation parentTransformation = nameToClassTransformation
                .get(parentClassname);

        // TAPESTRY-2449: Ignore the base class that Groovy can inject

        if (parentTransformation == null && !(parentClassname.equals("java.lang.Object") || parentClassname.equals(
                "groovy.lang.GroovyObjectSupport")))
        {
            String suggestedPackageName = buildSuggestedPackageName(classname);

            throw new RuntimeException(
                    ServicesMessages.baseClassInWrongPackage(parentClassname, classname, suggestedPackageName));
        }

        // TODO: Check that the name is not already in the map. But I think that can't happen,
        // because the classloader itself is synchronized.

        Resource baseResource = new ClasspathResource(classname.replace(".", "/") + ".class");

        ComponentModel parentModel = nameToComponentModel.get(parentClassname);

        MutableComponentModel model = new MutableComponentModelImpl(classname, logger, baseResource, parentModel);

        InternalClassTransformation transformation =
                parentTransformation == null
                ? new InternalClassTransformationImpl(classFactory, ctClass, componentClassCache, model, classSource)
                : parentTransformation.createChildTransformation(ctClass, model);

        try
        {
            workerChain.transform(transformation, model);

            transformation.finish();
        }
        catch (Throwable ex)
        {
            throw new TransformationException(transformation, ex);
        }

        transformLogger.debug(TapestryMarkers.CLASS_TRANSFORMATION, "Finished class transformation: {}",
                              transformation);

        nameToClassTransformation.put(classname, transformation);
        nameToComponentModel.put(classname, model);
    }

    public Instantiator createInstantiator(String componentClassName)
    {
        InternalClassTransformation ct = nameToClassTransformation.get(componentClassName);

        if (ct == null) throw new RuntimeException(ServicesMessages.classNotTransformed(componentClassName));

        try
        {
            return ct.createInstantiator();
        }
        catch (Throwable ex)
        {
            throw new TransformationException(ct, ex);
        }
    }

    private String buildSuggestedPackageName(String className)
    {
        for (String subpackage : SUBPACKAGES)
        {
            int pos = className.indexOf(subpackage);

            // Keep the leading '.' in the subpackage name and tack on "base".

            if (pos > 0) return className.substring(0, pos + 1) + InternalConstants.BASE_SUBPACKAGE;
        }

        // Is this even reachable?  className should always be in a controlled package and so
        // some subpackage above should have matched.

        return null;
    }
}
