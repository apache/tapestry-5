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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.internal.events.InvalidationListener;
import org.apache.tapestry.internal.util.MultiKey;
import org.apache.tapestry.ioc.AnnotationProvider;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newConcurrentMap;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;
import org.apache.tapestry.ioc.internal.util.GenericsUtils;
import org.apache.tapestry.ioc.services.*;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.services.ComponentLayer;
import org.apache.tapestry.services.PropertyConduitSource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.regex.Pattern;

public class PropertyConduitSourceImpl implements PropertyConduitSource, InvalidationListener
{
    private interface ReadInfo extends AnnotationProvider
    {
        /**
         * The name of the method to invoke.
         */
        String getMethodName();

        /**
         * The return type of the method, or the type of the property.
         */
        Class getType();

        /**
         * True if an explicit cast to the return type is needed (typically because of generics).
         */
        boolean isCastRequired();
    }


    /**
     * Result from writing the property navigation portion of the expression.  For getter methods, the navigation is all
     * terms in the expression; for setter methods, the navigation is all but the last term.
     */
    private interface PropertyNavigationResult
    {
        /**
         * The name of the variable holding the final step in the expression.
         */
        String getFinalStepVariable();

        /**
         * The type of the final step variable.
         */
        Class getFinalStepType();

        /**
         * The method read information for the final term in the navigation portion of the expression.
         */
        ReadInfo getFinalReadInfo();
    }

    private static final String PARENS = "()";

    private final PropertyAccess _access;

    private final ClassFactory _classFactory;

    private final Map<Class, Class> _classToEffectiveClass = newConcurrentMap();

    /**
     * Keyed on combination of root class and expression.
     */
    private final Map<MultiKey, PropertyConduit> _cache = newConcurrentMap();

    private static final MethodSignature GET_SIGNATURE = new MethodSignature(Object.class, "get",
                                                                             new Class[] { Object.class }, null);

    private static final MethodSignature SET_SIGNATURE = new MethodSignature(void.class, "set",
                                                                             new Class[] { Object.class, Object.class },
                                                                             null);

    private final Pattern SPLIT_AT_DOTS = Pattern.compile("\\.");

    public PropertyConduitSourceImpl(PropertyAccess access, @ComponentLayer ClassFactory classFactory)
    {
        _access = access;
        _classFactory = classFactory;
    }

    public PropertyConduit create(Class rootClass, String expression)
    {
        notNull(rootClass, "rootClass");
        notBlank(expression, "expression");

        Class effectiveClass = toEffectiveClass(rootClass);

        MultiKey key = new MultiKey(effectiveClass, expression);

        PropertyConduit result = _cache.get(key);

        if (result == null)
        {
            result = build(effectiveClass, expression);
            _cache.put(key, result);

        }

        return result;
    }

    private Class toEffectiveClass(Class rootClass)
    {
        Class result = _classToEffectiveClass.get(rootClass);

        if (result == null)
        {
            result = _classFactory.importClass(rootClass);

            _classToEffectiveClass.put(rootClass, result);
        }

        return result;
    }

    /**
     * Clears its caches when the component class loader is invalidated; this is because it will be common to generate
     * conduits rooted in a component class (which will no longer be valid and must be released to the garbage
     * collector).
     */
    public void objectWasInvalidated()
    {
        _cache.clear();
        _classToEffectiveClass.clear();
    }


    /**
     * Builds a subclass of {@link BasePropertyConduit} that implements the get() and set() methods and overrides the
     * constructor. In a worst-case race condition, we may build two (or more) conduits for the same
     * rootClass/expression, and it will get sorted out when the conduit is stored into the cache.
     *
     * @param rootClass
     * @param expression
     * @return the conduit
     */
    private PropertyConduit build(Class rootClass, String expression)
    {
        String name = ClassFabUtils.generateClassName("PropertyConduit");

        ClassFab classFab = _classFactory.newClass(name, BasePropertyConduit.class);

        classFab.addConstructor(new Class[] { Class.class, AnnotationProvider.class, String.class }, null,
                                "super($$);");

        String[] terms = SPLIT_AT_DOTS.split(expression);

        final ReadInfo readInfo = buildGetter(rootClass, classFab, expression, terms);
        final Method writeMethod = buildSetter(rootClass, classFab, expression, terms);

        // A conduit is either readable or writable, otherwise there will already have been
        // an error about unknown method name or property name.

        Class propertyType = readInfo != null ? readInfo.getType() : writeMethod
                .getParameterTypes()[0];

        String description = String.format("PropertyConduit[%s %s]", rootClass.getName(), expression);

        Class conduitClass = classFab.createClass();

        AnnotationProvider provider = new AnnotationProvider()
        {
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                T result = readInfo == null ? null : readInfo.getAnnotation(annotationClass);

                if (result == null && writeMethod != null) result = writeMethod.getAnnotation(annotationClass);

                return result;
            }

        };

        try
        {
            return (PropertyConduit) conduitClass.getConstructors()[0].newInstance(propertyType, provider, description);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

    }

    private ReadInfo buildGetter(Class rootClass, ClassFab classFab, String expression, String[] terms)
    {
        BodyBuilder builder = new BodyBuilder();

        builder.begin();

        PropertyNavigationResult result = writePropertyNavigationCode(builder, rootClass, expression, terms, false);


        if (result == null)
        {
            builder.clear();
            builder
                    .addln("throw new RuntimeException(\"Expression %s for class %s is write-only.\");", expression,
                           rootClass.getName());
        }
        else
        {
            builder.addln("return %s;", result.getFinalStepVariable());

            builder.end();
        }

        classFab.addMethod(Modifier.PUBLIC, GET_SIGNATURE, builder.toString());


        return result == null ? null : result.getFinalReadInfo();
    }

    /**
     * Writes the code for navigation
     *
     * @param builder
     * @param rootClass
     * @param expression
     * @param terms
     * @param forSetter  if true, then the last term is not read since it will be updated
     * @return
     */
    private PropertyNavigationResult writePropertyNavigationCode(BodyBuilder builder, Class rootClass,
                                                                 String expression, String[] terms, boolean forSetter)
    {
        builder.addln("%s root = (%<s) $1;", ClassFabUtils.toJavaClassName(rootClass));
        String previousStep = "root";

        Class activeType = rootClass;
        ReadInfo readInfo = null;

        // For a setter method, the navigation stops with  the penultimate
        // term in the expression (the final term is what gets updated).

        int lastIndex = forSetter ? terms.length - 1 : terms.length;

        for (int i = 0; i < lastIndex; i++)
        {
            String thisStep = "step" + (i + 1);
            String term = terms[i];

            boolean nullable = term.endsWith("?");
            if (nullable) term = term.substring(0, term.length() - 1);

            // All the navigation terms in the expression must be readable properties.
            // The only exception is the final term in a reader method.

            boolean mustExist = forSetter || i < terms.length - 1;

            readInfo = readInfoForTerm(activeType, expression, term, mustExist);

            // Means the property for this step exists but is write only, which is a problem!
            // This can only happen for getter methods, we return null to indicate that
            // the expression is write-only.

            if (readInfo == null) return null;

            // If a primitive type, convert to wrapper type

            Class termType = readInfo.getType();
            Class wrappedType = ClassFabUtils.getWrapperType(termType);

            String termJavaName = ClassFabUtils.toJavaClassName(wrappedType);
            builder.add("%s %s = ", termJavaName, thisStep);

            // Casts are needed for primitives, and for the case where
            // generics are involved.

            if (termType.isPrimitive())
            {
                builder.add(" ($w) ");
            }
            else if (readInfo.isCastRequired())
            {
                builder.add(" (%s) ", termJavaName);
            }

            builder.addln("%s.%s();", previousStep, readInfo.getMethodName());

            if (nullable)
            {
                builder.add("if (%s == null) return", thisStep);

                if (!forSetter) builder.add(" null");

                builder.addln(";");
            }

            activeType = wrappedType;
            previousStep = thisStep;
        }

        final String finalStepVariable = previousStep;
        final Class finalStepType = activeType;
        final ReadInfo finalReadInfo = readInfo;

        return new PropertyNavigationResult()
        {
            public String getFinalStepVariable()
            {
                return finalStepVariable;
            }

            public Class getFinalStepType()
            {
                return finalStepType;
            }

            public ReadInfo getFinalReadInfo()
            {
                return finalReadInfo;
            }
        };
    }

    private Method buildSetter(Class rootClass, ClassFab classFab, String expression, String[] terms)
    {
        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        PropertyNavigationResult result = writePropertyNavigationCode(builder, rootClass, expression, terms, true);

        // Because we pass true for the forSetter parameter, we know that the expression for the leading
        // terms is a chain of readable expressions.  But is the final term writable?

        Method writeMethod = writeMethodForTerm(result.getFinalStepType(), expression, terms[terms.length - 1]);

        if (writeMethod == null)
        {
            builder.clear();
            builder
                    .addln("throw new RuntimeException(\"Expression %s for class %s is read-only.\");", expression,
                           rootClass.getName());
            classFab.addMethod(Modifier.PUBLIC, SET_SIGNATURE, builder.toString());

            return null;
        }

        Class parameterType = writeMethod.getParameterTypes()[0];

        Class wrapperType = ClassFabUtils.getWrapperType(parameterType);

        // Cast the parameter from Object to the expected type for the method.

        builder.addln("%s value = (%<s) $2;", ClassFabUtils.toJavaClassName(wrapperType));

        // Invoke the method, possibly converting a wrapper type to a primitive type along the way.

        builder.add("%s.%s(value", result.getFinalStepVariable(), writeMethod.getName());

        if (parameterType != wrapperType)
            builder.add(".%s()", ClassFabUtils.getUnwrapMethodName(parameterType.getName()));

        builder.addln(");");

        builder.end();

        classFab.addMethod(Modifier.PUBLIC, SET_SIGNATURE, builder.toString());

        return writeMethod;
    }

    private Method writeMethodForTerm(Class activeType, String expression, String term)
    {
        if (term.endsWith(PARENS)) return null;

        ClassPropertyAdapter classAdapter = _access.getAdapter(activeType);
        PropertyAdapter adapter = classAdapter.getPropertyAdapter(term);

        if (adapter == null) throw new RuntimeException(
                ServicesMessages.noSuchProperty(activeType, term, expression, classAdapter.getPropertyNames()));

        return adapter.getWriteMethod();
    }

    private ReadInfo readInfoForTerm(Class activeType, String expression, String term, boolean mustExist)
    {
        if (term.endsWith(PARENS))
        {
            String methodName = term.substring(0, term.length() - PARENS.length());

            try
            {
                final Method method = findMethod(activeType, methodName);

                if (method.getReturnType().equals(void.class))
                    throw new RuntimeException(ServicesMessages.methodIsVoid(term, activeType, expression));


                final Class genericType = GenericsUtils.extractGenericReturnType(activeType, method);

                return new ReadInfo()
                {
                    public String getMethodName()
                    {
                        return method.getName();
                    }

                    public Class getType()
                    {
                        return genericType;
                    }

                    public boolean isCastRequired()
                    {
                        return genericType != method.getReturnType();
                    }

                    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
                    {
                        return method.getAnnotation(annotationClass);
                    }
                };

            }
            catch (NoSuchMethodException ex)
            {
                throw new RuntimeException(ServicesMessages.methodNotFound(term, activeType, expression), ex);
            }

        }

        // Otherwise, just a property name.

        ClassPropertyAdapter classAdapter = _access.getAdapter(activeType);
        final PropertyAdapter adapter = classAdapter.getPropertyAdapter(term);

        if (adapter == null) throw new RuntimeException(
                ServicesMessages.noSuchProperty(activeType, term, expression, classAdapter.getPropertyNames()));

        if (!adapter.isRead())
        {
            if (mustExist) throw new RuntimeException(ServicesMessages.writeOnlyProperty(term, activeType, expression));

            return null;
        }

        return new ReadInfo()
        {
            public String getMethodName()
            {
                return adapter.getReadMethod().getName();
            }

            public Class getType()
            {
                return adapter.getType();
            }

            public boolean isCastRequired()
            {
                return adapter.isCastRequired();
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return adapter.getAnnotation(annotationClass);
            }
        };
    }

    private Method findMethod(Class activeType, String methodName) throws NoSuchMethodException
    {
        for (Method method : activeType.getMethods())
        {

            if (method.getParameterTypes().length == 0 && method.getName().equalsIgnoreCase(methodName)) return method;

        }

        throw new NoSuchMethodException(ServicesMessages.noSuchMethod(activeType, methodName));
    }
}
