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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newConcurrentMap;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.internal.events.InvalidationListener;
import org.apache.tapestry.internal.util.MultiKey;
import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFabUtils;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.MethodSignature;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.ioc.services.PropertyAdapter;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.services.PropertyConduitSource;

public class PropertyConduitSourceImpl implements PropertyConduitSource, InvalidationListener
{
    private static final String PARENS = "()";

    private final PropertyAccess _access;

    private final ClassFactory _classFactory;

    private final Map<Class, Class> _classToEffectiveClass = newConcurrentMap();

    /** Keyed on combination of root class and expression. */
    private final Map<MultiKey, PropertyConduit> _cache = newConcurrentMap();

    private static final MethodSignature GET_SIGNATURE = new MethodSignature(Object.class, "get",
            new Class[]
            { Object.class }, null);

    private static final MethodSignature SET_SIGNATURE = new MethodSignature(void.class, "set",
            new Class[]
            { Object.class, Object.class }, null);

    public PropertyConduitSourceImpl(final PropertyAccess access, final ClassFactory classFactory)
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
     * Clears its cache when the component class loader is invalidated; this is because it will be
     * common to generated conduits rooted in a component class (which will no longer be valid and
     * must be released to the garbage collector).
     */
    public void objectWasInvalidated()
    {
        _cache.clear();
        _classToEffectiveClass.clear();
    }

    /**
     * Builds a subclass of {@link BasePropertyConduit} that implements the get() and set() methods
     * and overrides the constructor. In a worst-case race condition, we may build two (or more)
     * conduits for the same rootClass/expression, and it will get sorted out when the conduit is
     * stored into the cache.
     * 
     * @param rootClass
     * @param expression
     * @return the conduit
     */
    private PropertyConduit build(Class rootClass, String expression)
    {
        String name = ClassFabUtils.generateClassName("PropertyConduit");

        ClassFab classFab = _classFactory.newClass(name, BasePropertyConduit.class);

        classFab.addConstructor(new Class[]
        { Class.class, AnnotationProvider.class, String.class }, null, "super($$);");

        String[] terms = expression.split("\\.");

        final Method readMethod = buildGetter(rootClass, classFab, expression, terms);
        final Method writeMethod = buildSetter(rootClass, classFab, expression, terms);

        // A conduit is either readable or writeable, otherwise there will already have been
        // an error about unknown method name or property name.

        Class propertyType = readMethod != null ? readMethod.getReturnType() : writeMethod
                .getParameterTypes()[0];

        String description = String.format(
                "PropertyConduit[%s %s]",
                rootClass.getName(),
                expression);

        Class conduitClass = classFab.createClass();

        AnnotationProvider provider = new AnnotationProvider()
        {

            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                T result = readMethod == null ? null : readMethod.getAnnotation(annotationClass);

                if (result == null && writeMethod != null)
                    result = writeMethod.getAnnotation(annotationClass);

                return result;
            }

        };

        try
        {
            return (PropertyConduit) conduitClass.getConstructors()[0].newInstance(
                    propertyType,
                    provider,
                    description);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

    }

    private Method buildGetter(Class rootClass, ClassFab classFab, String expression, String[] terms)
    {
        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        builder.addln("%s root = (%<s) $1;", ClassFabUtils.toJavaClassName(rootClass));
        String previousStep = "root";

        Class activeType = rootClass;
        Method result = null;
        boolean writeOnly = false;

        for (int i = 0; i < terms.length; i++)
        {
            String thisStep = "step" + (i + 1);
            String term = terms[i];

            boolean nullable = term.endsWith("?");
            if (nullable) term = term.substring(0, term.length() - 1);

            Method readMethod = readMethodForTerm(
                    activeType,
                    expression,
                    term,
                    (i < terms.length - 1));

            if (readMethod == null)
            {
                writeOnly = true;
                break;
            }

            // If a primitive type, convert to wrapper type

            Class termType = ClassFabUtils.getWrapperType(readMethod.getReturnType());

            // $w is harmless for non-wrapper types.

            builder.addln(
                    "%s %s = ($w) %s.%s();",
                    ClassFabUtils.toJavaClassName(termType),
                    thisStep,
                    previousStep,
                    readMethod.getName());

            if (nullable) builder.addln("if (%s == null) return null;", thisStep);

            activeType = termType;
            result = readMethod;
            previousStep = thisStep;
        }

        builder.addln("return %s;", previousStep);

        builder.end();

        if (writeOnly)
        {
            builder.clear();
            builder
                    .addln(
                            "throw new java.lang.RuntimeException(\"Expression %s for class %s is write-only.\");",
                            expression,
                            rootClass.getName());
        }

        classFab.addMethod(Modifier.PUBLIC, GET_SIGNATURE, builder.toString());

        return result;
    }

    private Method buildSetter(Class rootClass, ClassFab classFab, String expression, String[] terms)
    {
        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        builder.addln("%s root = (%<s) $1;", ClassFabUtils.toJavaClassName(rootClass));
        String previousStep = "root";

        Class activeType = rootClass;

        for (int i = 0; i < terms.length - 1; i++)
        {
            String thisStep = "step" + (i + 1);
            String term = terms[i];

            boolean nullable = term.endsWith("?");
            if (nullable) term = term.substring(0, term.length() - 1);

            Method readMethod = readMethodForTerm(activeType, expression, term, true);

            // If a primitive type, convert to wrapper type

            Class termType = ClassFabUtils.getWrapperType(readMethod.getReturnType());

            // $w is harmless for non-wrapper types.

            builder.addln(
                    "%s %s = ($w) %s.%s();",
                    ClassFabUtils.toJavaClassName(termType),
                    thisStep,
                    previousStep,
                    readMethod.getName());

            if (nullable) builder.addln("if (%s == null) return;", thisStep);

            activeType = termType;
            previousStep = thisStep;
        }

        // When writing, the last step is different.

        Method writeMethod = writeMethodForTerm(activeType, expression, terms[terms.length - 1]);

        if (writeMethod == null)
        {
            builder.clear();
            builder
                    .addln(
                            "throw new java.lang.RuntimeException(\"Expression %s for class %s is read-only.\");",
                            expression,
                            rootClass.getName());
            classFab.addMethod(Modifier.PUBLIC, SET_SIGNATURE, builder.toString());

            return null;
        }

        Class parameterType = writeMethod.getParameterTypes()[0];

        Class wrapperType = ClassFabUtils.getWrapperType(parameterType);

        builder.addln("%s value = (%<s) $2;", ClassFabUtils.toJavaClassName(wrapperType));

        builder.add("%s.%s(value", previousStep, writeMethod.getName());

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

        PropertyAdapter adapter = _access.getAdapter(activeType).getPropertyAdapter(term);

        if (adapter == null)
            throw new RuntimeException(ServicesMessages
                    .noSuchProperty(activeType, term, expression));

        return adapter.getWriteMethod();
    }

    private Method readMethodForTerm(Class activeType, String expression, String term,
            boolean mustExist)
    {
        if (term.endsWith(PARENS))
        {
            Method method = null;
            String methodName = term.substring(0, term.length() - PARENS.length());

            try
            {
                method = activeType.getMethod(methodName);
            }
            catch (NoSuchMethodException ex)
            {
                throw new RuntimeException(ServicesMessages.methodNotFound(
                        term,
                        activeType,
                        expression), ex);
            }

            if (method.getReturnType().equals(void.class))
                throw new RuntimeException(ServicesMessages.methodIsVoid(
                        term,
                        activeType,
                        expression));

            return method;
        }

        PropertyAdapter adapter = _access.getAdapter(activeType).getPropertyAdapter(term);

        if (adapter == null)
            throw new RuntimeException(ServicesMessages
                    .noSuchProperty(activeType, term, expression));

        Method m = adapter.getReadMethod();

        if (m == null && mustExist)
            throw new RuntimeException(ServicesMessages.writeOnlyProperty(
                    term,
                    activeType,
                    expression));

        return m;
    }
}
