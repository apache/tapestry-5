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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.internal.events.InvalidationListener;
import org.apache.tapestry5.internal.util.MultiKey;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.GenericsUtils;
import org.apache.tapestry5.ioc.services.*;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.services.ComponentLayer;
import org.apache.tapestry5.services.PropertyConduitSource;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.regex.Pattern;

public class PropertyConduitSourceImpl implements PropertyConduitSource, InvalidationListener
{
    private interface ExpressionTermInfo extends AnnotationProvider
    {
        /**
         * The name of the method to invoke to read the property value, or null.
         */
        String getReadMethodName();

        /**
         * The name of the method to invoke to write the property value, or null.
         */
        String getWriteMethodName();

        /**
         * The return type of the method, or the type of the property.
         */
        Class getType();

        /**
         * True if an explicit cast to the return type is needed (typically because of generics).
         */
        boolean isCastRequired();
    }


    private static final String PARENS = "()";

    private final PropertyAccess access;

    private final ClassFactory classFactory;

    private final Map<Class, Class> classToEffectiveClass = CollectionFactory.newConcurrentMap();

    /**
     * Keyed on combination of root class and expression.
     */
    private final Map<MultiKey, PropertyConduit> cache = CollectionFactory.newConcurrentMap();

    private static final MethodSignature GET_SIGNATURE = new MethodSignature(Object.class, "get",
                                                                             new Class[] { Object.class }, null);

    private static final MethodSignature SET_SIGNATURE = new MethodSignature(void.class, "set",
                                                                             new Class[] { Object.class, Object.class },
                                                                             null);

    private final Pattern SPLIT_AT_DOTS = Pattern.compile("\\.");

    public PropertyConduitSourceImpl(PropertyAccess access, @ComponentLayer ClassFactory classFactory)
    {
        this.access = access;
        this.classFactory = classFactory;
    }

    public PropertyConduit create(Class rootClass, String expression)
    {
        Defense.notNull(rootClass, "rootClass");
        Defense.notBlank(expression, "expression");

        Class effectiveClass = toEffectiveClass(rootClass);

        MultiKey key = new MultiKey(effectiveClass, expression);

        PropertyConduit result = cache.get(key);

        if (result == null)
        {
            result = build(effectiveClass, expression);
            cache.put(key, result);

        }

        return result;
    }

    private Class toEffectiveClass(Class rootClass)
    {
        Class result = classToEffectiveClass.get(rootClass);

        if (result == null)
        {
            result = classFactory.importClass(rootClass);

            classToEffectiveClass.put(rootClass, result);
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
        cache.clear();
        classToEffectiveClass.clear();
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

        ClassFab classFab = classFactory.newClass(name, BasePropertyConduit.class);

        classFab.addConstructor(new Class[] { Class.class, AnnotationProvider.class, String.class }, null,
                                "super($$);");

        String[] terms = SPLIT_AT_DOTS.split(expression);

        MethodSignature navigate = createNavigationMethod(rootClass, classFab, expression, terms);

        String lastTerm = terms[terms.length - 1];

        ExpressionTermInfo termInfo = infoForTerm(navigate.getReturnType(), expression, lastTerm);

        createAccessors(rootClass, expression, classFab, navigate, termInfo);

        String description = String.format("PropertyConduit[%s %s]", rootClass.getName(), expression);

        Class conduitClass = classFab.createClass();

        try
        {
            return (PropertyConduit) conduitClass.getConstructors()[0].newInstance(termInfo.getType(), termInfo,
                                                                                   description);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void createAccessors(Class rootClass, String expression, ClassFab classFab, MethodSignature navigateMethod,
                                 ExpressionTermInfo termInfo)
    {
        createGetter(rootClass, expression, classFab, navigateMethod, termInfo);
        createSetter(rootClass, expression, classFab, navigateMethod, termInfo);
    }

    private void createSetter(Class rootClass, String expression, ClassFab classFab, MethodSignature navigateMethod,
                              ExpressionTermInfo termInfo)
    {
        String methodName = termInfo.getWriteMethodName();

        if (methodName == null)
        {
            createNoOp(classFab, SET_SIGNATURE, "Expression %s for class %s is read-only.", expression,
                       rootClass.getName());
            return;
        }

        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("%s target = %s($1);",
                      ClassFabUtils.toJavaClassName(navigateMethod.getReturnType()),
                      navigateMethod.getName());

        // I.e. due to ?. operator

        builder.addln("if (target == null) return;");

        String propertyTypeName = ClassFabUtils.toJavaClassName(termInfo.getType());

        builder.addln("target.%s(%s);", methodName, ClassFabUtils.castReference("$2", propertyTypeName));

        builder.end();

        classFab.addMethod(Modifier.PUBLIC, SET_SIGNATURE, builder.toString());
    }

    private void createGetter(Class rootClass, String expression, ClassFab classFab, MethodSignature navigateMethod,
                              ExpressionTermInfo termInfo)
    {
        String methodName = termInfo.getReadMethodName();

        if (methodName == null)
        {
            createNoOp(classFab, GET_SIGNATURE, "Expression %s for class %s is write-only.", expression,
                       rootClass.getName());
            return;
        }

        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("%s target = %s($1);", ClassFabUtils.toJavaClassName(navigateMethod.getReturnType()),
                      navigateMethod.getName());

        // I.e. due to ?. operator

        builder.addln("if (target == null) return null;");

        builder.addln("return ($w) target.%s();", methodName);

        builder.end();

        classFab.addMethod(Modifier.PUBLIC, GET_SIGNATURE, builder.toString());
    }


    private void createNoOp(ClassFab classFab, MethodSignature signature, String format, Object... values)
    {
        String message = String.format(format, values);

        String body = String.format("throw new RuntimeException(\"%s\");", message);

        classFab.addMethod(Modifier.PUBLIC, signature, body);
    }


    /**
     * Builds a method that navigates from the root object upto, but not including, the final property. For simple
     * properties, the generated method is effectively a big cast.  Otherwise, the generated method returns the object
     * that contains the final property (the final term).       The generated method may return null if an intermediate
     * term is null (and evaluated using the "?." safe dereferencing operator).
     *
     * @param rootClass
     * @param classFab
     * @param expression
     * @param terms      the expression divided into individual terms
     * @return signature of the added method
     */
    private MethodSignature createNavigationMethod(Class rootClass, ClassFab classFab, String expression,
                                                   String[] terms)
    {
        BodyBuilder builder = new BodyBuilder().begin();

        builder.addln("%s root = (%<s) $1;", ClassFabUtils.toJavaClassName(rootClass));
        String previousStep = "root";

        builder.addln(
                "if (root == null) throw new NullPointerException(\"Root object of property expression '%s' is null.\");",
                expression);

        Class activeType = rootClass;
        ExpressionTermInfo expressionTermInfo = null;

        for (int i = 0; i < terms.length - 1; i++)
        {
            String thisStep = "step" + (i + 1);
            String term = terms[i];

            boolean nullable = term.endsWith("?");

            if (nullable) term = term.substring(0, term.length() - 1);

            expressionTermInfo = infoForTerm(activeType, expression, term);

            String methodName = expressionTermInfo.getReadMethodName();

            if (methodName == null)
                throw new RuntimeException(ServicesMessages.writeOnlyProperty(term, activeType, expression));

            // If a primitive type, convert to wrapper type

            Class termType = expressionTermInfo.getType();
            Class wrappedType = ClassFabUtils.getWrapperType(termType);

            String termJavaName = ClassFabUtils.toJavaClassName(wrappedType);
            builder.add("%s %s = ", termJavaName, thisStep);

            // Casts are needed for primitives, and for the case where
            // generics are involved.

            if (termType.isPrimitive())
            {
                builder.add(" ($w) ");
            }
            else if (expressionTermInfo.isCastRequired())
            {
                builder.add(" (%s) ", termJavaName);
            }

            builder.addln("%s.%s();", previousStep, expressionTermInfo.getReadMethodName());

            if (nullable)
            {
                builder.add("if (%s == null) return null;", thisStep);
            }
            else
            {
                // Perform a null check on intermediate terms.
                builder.addln("if (%s == null) %s.nullTerm(\"%s\", \"%s\", root);",
                              thisStep, getClass().getName(), term, expression);
            }

            activeType = wrappedType;
            previousStep = thisStep;
        }

        builder.addln("return %s;", previousStep);

        builder.end();

        MethodSignature sig = new MethodSignature(activeType, "navigate", new Class[] { Object.class }, null);

        classFab.addMethod(Modifier.PRIVATE, sig, builder.toString());

        return sig;
    }


    private ExpressionTermInfo infoForTerm(Class activeType, String expression, String term)
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

                return new ExpressionTermInfo()
                {
                    public String getReadMethodName()
                    {
                        return method.getName();
                    }

                    public String getWriteMethodName()
                    {
                        return null;
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

        ClassPropertyAdapter classAdapter = access.getAdapter(activeType);
        final PropertyAdapter adapter = classAdapter.getPropertyAdapter(term);

        if (adapter == null) throw new RuntimeException(
                ServicesMessages.noSuchProperty(activeType, term, expression, classAdapter.getPropertyNames()));

        return new ExpressionTermInfo()
        {
            public String getReadMethodName()
            {
                return name(adapter.getReadMethod());
            }

            public String getWriteMethodName()
            {
                return name(adapter.getWriteMethod());
            }

            private String name(Method m)
            {
                return m == null ? null : m.getName();
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

    /**
     * May be invoked from the fabricated PropertyConduit instances.
     */
    public static void nullTerm(String term, String expression, Object root)
    {
        String message = String.format("Property '%s' (within property expression '%s', of %s) is null.",
                                       term, expression, root);

        throw new NullPointerException(message);
    }
}
